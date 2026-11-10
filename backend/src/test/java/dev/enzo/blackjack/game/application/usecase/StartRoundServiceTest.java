package dev.enzo.blackjack.game.application.usecase;

import dev.enzo.blackjack.game.application.port.out.DeckFactory;
import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.*;
import dev.enzo.blackjack.player.application.exception.PlayerNotFoundException;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.domain.model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StartRoundServiceTest {

    private PlayerRepository playerRepository;
    private RoundRepository roundRepository;
    private RoundEventPublisher eventPublisher;
    private DeckFactory deckFactory;
    private StartRoundService service;

    @BeforeEach
    void setUp() {
        playerRepository = mock(PlayerRepository.class);
        roundRepository = mock(RoundRepository.class);
        eventPublisher = mock(RoundEventPublisher.class);
        deckFactory = mock(DeckFactory.class);

        when(roundRepository.save(any(Round.class))).thenReturn(Mono.empty());
        service = new StartRoundService(playerRepository, roundRepository, eventPublisher, deckFactory);
    }

    private Deck deckWithCards(Card... cards) {
        Deck deck = mock(Deck.class);
        when(deck.cardDraw()).thenReturn(cards[0], Arrays.copyOfRange(cards, 1, cards.length));

        return deck;
    }

    private void givenExistingPlayer(UUID playerId) {
        Player player = new Player(playerId, "Enzo");
        when(playerRepository.findById(playerId)).thenReturn(Mono.just(player));
    }

    private void giveDeckWithoutBlackjack() {
        when(deckFactory.createDeck()).thenAnswer(invocation ->
                deckWithCards(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.HEARTS, Rank.NINE),
                        new Card(Suit.DIAMONDS, Rank.SEVEN), new Card(Suit.CLUBS, Rank.EIGHT)));
    }

    @Test
    void shouldStartAndSaveRoundForExistingPlayer() {
        UUID playerId = UUID.randomUUID();
        givenExistingPlayer(playerId);
        giveDeckWithoutBlackjack();

        ArgumentCaptor<Round> captor = ArgumentCaptor.forClass(Round.class);

        StepVerifier.create(service.startRound(playerId)).assertNext(roundId -> {
            assertNotNull(roundId);

            verify(roundRepository).save(captor.capture());

            Round savedRound = captor.getValue();

            assertEquals(roundId, savedRound.getRoundId());
            assertEquals(playerId, savedRound.getPlayerId());
            assertEquals(RoundStatus.PLAYER_TURN, savedRound.getStatus());
        }).verifyComplete();

        verify(playerRepository).findById(playerId);
        verify(deckFactory).createDeck();
    }

    @Test
    void shouldRejectNonExistingPlayer() {
        UUID playerId = UUID.randomUUID();

        when(playerRepository.findById(playerId)).thenReturn(Mono.empty());

        StepVerifier.create(service.startRound(playerId)).expectError(PlayerNotFoundException.class).verify();

        verify(roundRepository, never()).save(any(Round.class));
        verifyNoInteractions(deckFactory, eventPublisher);
    }

    @Test
    void shouldRejectNullPlayerId() {
        StepVerifier.create(service.startRound(null)).expectError(NullPointerException.class).verify();

        verifyNoInteractions(playerRepository, eventPublisher, deckFactory);
        verify(roundRepository, never()).save(any(Round.class));
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentRounds() {
        UUID playerId = UUID.randomUUID();

        givenExistingPlayer(playerId);
        giveDeckWithoutBlackjack();

        UUID firstRoundId = service.startRound(playerId).block();
        UUID secondRoundId = service.startRound(playerId).block();

        assertNotNull(firstRoundId);
        assertNotNull(secondRoundId);
        assertNotEquals(firstRoundId, secondRoundId);
        verify(roundRepository, times(2)).save(any(Round.class));
        verify(deckFactory, times(2)).createDeck();
    }

    @Test
    void shouldRejectNullDependencies() {
        assertThrows(NullPointerException.class,
                () -> new StartRoundService(null, roundRepository, eventPublisher, deckFactory));

        assertThrows(NullPointerException.class,
                () -> new StartRoundService(playerRepository, null, eventPublisher, deckFactory));

        assertThrows(NullPointerException.class,
                () -> new StartRoundService(playerRepository, roundRepository, null, deckFactory));

        assertThrows(NullPointerException.class,
                () -> new StartRoundService(playerRepository, roundRepository, eventPublisher, null));
    }

    @Test
    void shouldPublishEventWhenNaturalBlackjackFinishesRound() {
        UUID playerId = UUID.randomUUID();

        givenExistingPlayer(playerId);

        Deck deck = deckWithCards(new Card(Suit.HEARTS, Rank.AS), new Card(Suit.SPADES, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.HEARTS, Rank.SEVEN));

        when(deckFactory.createDeck()).thenReturn(deck);

        when(eventPublisher.publish(any(RoundFinishedEvent.class))).thenReturn(Mono.empty());

        ArgumentCaptor<RoundFinishedEvent> captor = ArgumentCaptor.forClass(RoundFinishedEvent.class);

        StepVerifier.create(service.startRound(playerId)).assertNext(roundId -> {
            var inOrder = inOrder(roundRepository, eventPublisher);

            inOrder.verify(roundRepository).save(any(Round.class));
            inOrder.verify(eventPublisher).publish(captor.capture());
            RoundFinishedEvent event = captor.getValue();

            assertEquals(roundId, event.roundId());
            assertEquals(playerId, event.playerId());
            assertEquals(RoundResult.PLAYER_WIN, event.result());
        }).verifyComplete();
    }

    @Test
    void shouldNotPublishEventWhenRoundIsStillActive() {
        UUID playerId = UUID.randomUUID();

        givenExistingPlayer(playerId);
        giveDeckWithoutBlackjack();

        StepVerifier.create(service.startRound(playerId)).expectNextCount(1).verifyComplete();

        verify(roundRepository).save(any(Round.class));
        verifyNoInteractions(eventPublisher);
    }
}

