package dev.enzo.blackjack.game.integration;

import dev.enzo.blackjack.game.application.port.out.DeckFactory;
import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.application.usecase.StandRoundService;
import dev.enzo.blackjack.game.application.usecase.StartRoundService;
import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.*;
import dev.enzo.blackjack.game.infrastructure.fake.InMemoryRoundEventPublisher;
import dev.enzo.blackjack.game.infrastructure.fake.InMemoryRoundRepository;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.application.usecase.CreatePlayerService;
import dev.enzo.blackjack.player.application.usecase.RecordRoundResultService;
import dev.enzo.blackjack.player.domain.model.Player;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryPlayerRepository;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryRoundResultRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoundFlowIntegrationTest {

    @Test
    void shouldUpdatePlayerStatisticsWhenRoundFinishes() {
        PlayerRepository playerRepository = new InMemoryPlayerRepository();
        RoundRepository roundRepository = new InMemoryRoundRepository();

        InMemoryRoundResultRepository resultRepository = new InMemoryRoundResultRepository(playerRepository);

        RecordRoundResultService recordResultService = new RecordRoundResultService(resultRepository);

        RoundEventPublisher eventPublisher = new InMemoryRoundEventPublisher(recordResultService);

        DeckFactory deckFactory = Deck::new;

        CreatePlayerService createPlayerService = new CreatePlayerService(playerRepository);

        StartRoundService startRoundService = new StartRoundService(playerRepository, roundRepository, eventPublisher,
                deckFactory);

        StandRoundService standRoundService = new StandRoundService(roundRepository, eventPublisher);

        StepVerifier.create(createPlayerService.createPlayer("Enzo").flatMap(createdPlayer -> {
            UUID playerId = createdPlayer.playerId();

            return startRoundService.startRound(playerId)
                    .flatMap(roundId ->
                                    standRoundService.stand(roundId).then(Mono.zip(roundRepository
                                                            .findById(roundId),
                                                    playerRepository.findById(playerId)))
                                            .doOnNext(tuple -> {
                                                var round = tuple.getT1();
                                                var player = tuple.getT2();

                                                assertEquals(RoundStatus.FINISHED, round.getStatus());
                                                assertEquals(RoundResult.DEALER_WIN, round.getResult());

                                                assertEquals(0, player.getWins());
                                                assertEquals(1, player.getLosses());
                                                assertEquals(0, player.getDraws());
                                            }).then(eventPublisher.publish(new RoundFinishedEvent(roundId,
                                                                    playerId, RoundResult.DEALER_WIN)))
                                            .then(playerRepository.findById(playerId))
                                            .doOnNext(player -> assertEquals(1, player.getLosses())));
        })).expectNextCount(1).verifyComplete();
    }

    @Test
    void shouldRecordPlayerWinWhenDealerFinishesTurn() {
        PlayerRepository playerRepository = new InMemoryPlayerRepository();
        RoundRepository roundRepository = new InMemoryRoundRepository();

        InMemoryRoundResultRepository resultRepository = new InMemoryRoundResultRepository(playerRepository);

        RecordRoundResultService recordResultService = new RecordRoundResultService(resultRepository);

        RoundEventPublisher eventPublisher = new InMemoryRoundEventPublisher(recordResultService);

        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.EIGHT));

        DeckFactory deckFactory = () -> deck;

        CreatePlayerService createPlayerService = new CreatePlayerService(playerRepository);

        StartRoundService startRoundService = new StartRoundService(playerRepository, roundRepository, eventPublisher,
                deckFactory);

        StandRoundService standRoundService = new StandRoundService(roundRepository, eventPublisher);

        StepVerifier.create(createPlayerService.createPlayer("Enzo").flatMap(createdPlayer -> {
            UUID playerId = createdPlayer.playerId();

            return startRoundService.startRound(playerId)
                    .flatMap(roundId ->
                                    standRoundService.stand(roundId)
                                            .then(
                                                    Mono.zip(roundRepository.findById(roundId),
                                                            playerRepository.findById(playerId))));
        })).assertNext(tuple -> {
            var round = tuple.getT1();
            var player = tuple.getT2();

            assertEquals(RoundStatus.FINISHED, round.getStatus());
            assertEquals(RoundResult.PLAYER_WIN, round.getResult());

            assertEquals(1, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getDraws());
        }).verifyComplete();
    }

    @Test
    void shouldRecordDrawWhenPlayerAndDealerHaveSameScore() {
        PlayerRepository playerRepository = new InMemoryPlayerRepository();
        RoundRepository roundRepository = new InMemoryRoundRepository();

        InMemoryRoundResultRepository resultRepository = new InMemoryRoundResultRepository(playerRepository);

        RecordRoundResultService recordResultService = new RecordRoundResultService(resultRepository);

        RoundEventPublisher eventPublisher = new InMemoryRoundEventPublisher(recordResultService);

        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.SPADES, Rank.TEN), new Card(Suit.SPADES, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.EIGHT), new Card(Suit.CLUBS, Rank.NINE));

        DeckFactory deckFactory = () -> deck;

        CreatePlayerService createPlayerService = new CreatePlayerService(playerRepository);

        StartRoundService startRoundService = new StartRoundService(playerRepository, roundRepository, eventPublisher,
                deckFactory);

        StandRoundService standRoundService = new StandRoundService(roundRepository, eventPublisher);

        StepVerifier.create(createPlayerService.createPlayer("Enzo").flatMap(createdPlayer -> {
            UUID playerId = createdPlayer.playerId();

            return startRoundService.startRound(playerId)
                    .flatMap(roundId ->
                            standRoundService.stand(roundId)
                                    .then(
                                            Mono.zip(roundRepository.findById(roundId),
                                                    playerRepository.findById(playerId))));
        })).assertNext(tuple -> {
            var round = tuple.getT1();
            var player = tuple.getT2();

            assertEquals(RoundStatus.FINISHED, round.getStatus());
            assertEquals(RoundResult.PUSH, round.getResult());

            assertEquals(0, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(1, player.getDraws());
        }).verifyComplete();
    }


}
