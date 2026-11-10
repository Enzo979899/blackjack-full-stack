package dev.enzo.blackjack.game.domain.model;

import org.junit.jupiter.api.Test;
import org.mockito.configuration.IMockitoConfiguration;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoundTest {

    @Test
    void shouldCreateRoundWithEmptyHands() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        assertEquals(0, round.getDealerHand().getCardCount());
        assertEquals(0, round.getPlayerHand().getCardCount());
    }

    @Test
    void shouldCreateRoundWithoutResult() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        assertNull(round.getResult());
    }

    @Test
    void shouldRejectNullDeck() {
        assertThrows(NullPointerException.class, () -> new Round(UUID.randomUUID(), UUID.randomUUID(), null));
    }

    @Test
    void shouldCreateRoundWithInitialStatus() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        assertEquals(RoundStatus.WAITING_TO_START, round.getStatus());
    }

    @Test
    void shoulStartRound() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());
    }

    @Test
    void shoulNotStartRoundTwice() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        assertThrows(IllegalStateException.class, round::start);
    }

    @Test
    void shouldDelaTwoCardsToPlayerWhenRoundStarts() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        assertEquals(2, round.getPlayerHand().getCardCount());
    }

    @Test
    void shouldDealTwoCardsToDealerWhenRoundStarts() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        assertEquals(2, round.getDealerHand().getCardCount());
    }

    @Test
    void shouldRemoveFourCardsFromDeckAfterRoundStarts() {
        Deck deck = new Deck();
        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        assertEquals(48, deck.getRemainingCards());
    }

    @Test
    void shouldNotDrawMoreCardsWhenStartingRoundTwice() {
        Deck deck = new Deck();
        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        assertThrows(IllegalStateException.class, round::start);
        assertEquals(48, deck.getRemainingCards());
    }

    @Test
    void shouldAddCardToPlayerHandWhenHit() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        round.hit();

        assertEquals(3, round.getPlayerHand().getCardCount());
    }

    @Test
    void shouldNotHitBeforeRoundStarts() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        assertThrows(IllegalStateException.class, round::hit);
    }

    @Test
    void shouldFinishRoundWhenPlayerBusts() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        while (!round.getPlayerHand().isBusted()) {
            round.hit();
        }

        assertEquals(RoundStatus.FINISHED, round.getStatus());
        assertEquals(RoundResult.DEALER_WIN, round.getResult());
    }

    @Test
    void shouldNotAllowHitAfterPlayerBusts() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        while (!round.getPlayerHand().isBusted()) {
            round.hit();
        }

        assertThrows(IllegalStateException.class, round::hit);
    }

    @Test
    void shouldChangeToDealerTurnWhenPlayerStands() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        round.stand();

        assertEquals(RoundStatus.DEALER_TURN, round.getStatus());
    }

    @Test
    void shouldNotAllowHitAfterPlayerStands() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        round.stand();

        assertThrows(IllegalStateException.class, round::hit);
    }

    @Test
    void shouldNotAllowStandTwice() {
       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        round.stand();

        assertThrows(IllegalStateException.class, round::stand);
    }

    @Test
    void shouldDealerDrawCardWhenScoreIsBelow17() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.SIX),
                new Card(Suit.SPADES, Rank.SEVEN), new Card(Suit.SPADES, Rank.TEN), new Card(Suit.HEARTS, Rank.FOUR));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        assertEquals(17, round.getPlayerHand().calculateScore());
        assertEquals(16, round.getDealerHand().calculateScore());

        round.stand();

        round.playDealerTurn();

        assertEquals(3, round.getDealerHand().getCardCount());
        assertEquals(20, round.getDealerHand().calculateScore());
    }

    @Test
    void shouldDealerStandWhenScoreIs17() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.SEVEN),
                new Card(Suit.DIAMONDS, Rank.EIGHT), new Card(Suit.SPADES, Rank.TEN));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.stand();

        round.playDealerTurn();

        assertEquals(17, round.getDealerHand().calculateScore());
        assertEquals(2, round.getDealerHand().getCardCount());

        verify(deck, times(4)).cardDraw();
    }

    @Test
    void shouldPlayerWinswhenDealerBust() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.SIX),
                new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.SPADES, Rank.TEN), new Card(Suit.CLUBS, Rank.KING));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.stand();

        round.playDealerTurn();

        assertTrue(round.getDealerHand().isBusted());
        assertEquals(RoundStatus.FINISHED, round.getStatus());
        assertEquals(RoundResult.PLAYER_WIN, round.getResult());
    }

    @Test
    void shouldPlayerWinWhenScoreIsHigherThanDealer() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.SPADES, Rank.TEN), new Card(Suit.SPADES, Rank.EIGHT),
                new Card(Suit.DIAMONDS, Rank.TEN), new Card(Suit.HEARTS, Rank.TEN));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.stand();

        round.playDealerTurn();

        assertEquals(RoundResult.PLAYER_WIN, round.getResult());
        assertEquals(RoundStatus.FINISHED, round.getStatus());
    }

    @Test
    void shouldDealerWinWhenScoreIsHigherThanPlayer() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.DIAMONDS, Rank.TEN), new Card(Suit.SPADES, Rank.TEN),
                new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.SPADES, Rank.KING));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.stand();

        round.playDealerTurn();

        assertEquals(RoundResult.DEALER_WIN, round.getResult());
        assertEquals(RoundStatus.FINISHED, round.getStatus());

    }

    @Test
    void shouldEndRoundWithPushWhenScoresAreSame() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.SPADES, Rank.TEN), new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.EIGHT), new Card(Suit.CLUBS, Rank.EIGHT));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.stand();

        round.playDealerTurn();

        assertEquals(RoundResult.PUSH, round.getResult());
        assertEquals(RoundStatus.FINISHED, round.getStatus());
    }

    @Test
    void shouldFinishRoundWhenPlayerHasBlackJackAndDealerDoesNot() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.HEARTS, Rank.AS), new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.KING), new Card(Suit.HEARTS, Rank.SEVEN));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        assertEquals(RoundStatus.FINISHED, round.getStatus());
        assertEquals(RoundResult.PLAYER_WIN, round.getResult());
    }

    @Test
    void shouldEndRoundWithPushWhenBothHaveBlackJack() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.HEARTS, Rank.AS), new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.QUEEN), new Card(Suit.DIAMONDS, Rank.AS));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        assertEquals(RoundStatus.FINISHED, round.getStatus());
        assertEquals(RoundResult.PUSH, round.getResult());
    }

    @Test
    void shouldDealerWinWhenOnlyDealerHasBlackJack() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.HEARTS, Rank.AS),
                new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.DIAMONDS, Rank.KING));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        assertEquals(RoundStatus.FINISHED, round.getStatus());
        assertEquals(RoundResult.DEALER_WIN, round.getResult());
    }

    @Test
    void shouldNotCount21WithThreeCardsAsNaturalBlackJack() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.SPADES, Rank.FIVE), new Card(Suit.HEARTS, Rank.SEVEN), new Card(Suit.CLUBS, Rank.SIX));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.hit();

        assertEquals(21, round.getPlayerHand().calculateScore());
        assertEquals(3, round.getPlayerHand().getCardCount());
        assertFalse(round.getPlayerHand().isBlackJack());
        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());
        assertNull(round.getResult());
    }

    @Test
    void shouldNotAllowHitWhenPlayerHas21Points() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.FIVE), new Card(Suit.HEARTS, Rank.SEVEN), new Card(Suit.CLUBS, Rank.SIX));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.hit();

        assertEquals(21, round.getPlayerHand().calculateScore());
        assertThrows(IllegalStateException.class, round::hit);
    }

    @Test
    void shouldNotDrawCardWhenPlayerAlreadyHas21Points() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.SPADES, Rank.FIVE), new Card(Suit.HEARTS, Rank.SEVEN), new Card(Suit.CLUBS, Rank.SIX));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.hit();

        assertThrows(IllegalStateException.class, round::hit);
        assertEquals(3, round.getPlayerHand().getCardCount());
        assertEquals(21, round.getPlayerHand().calculateScore());
        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());

        verify(deck, times(5)).cardDraw();
    }

    @Test
    void shouldNotAllowDealerTurnWhileItIsPlayerTurn() {

       Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), new Deck());

        round.start();

        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());
        assertThrows(IllegalStateException.class, round::playDealerTurn);
        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());
        assertNull(round.getResult());
    }

    @Test
    void shouldNotAllowDealerToPlayTwice() {
        Deck deck = mock(Deck.class);

        when(deck.cardDraw()).thenReturn(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.DIAMONDS, Rank.KING));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        round.start();

        round.stand();

        round.playDealerTurn();

        assertEquals(RoundStatus.FINISHED, round.getStatus());
        assertEquals(RoundResult.DEALER_WIN, round.getResult());
        assertThrows(IllegalStateException.class, round::playDealerTurn);
        assertEquals(RoundStatus.FINISHED, round.getStatus());

        verify(deck, times(4)).cardDraw();
    }

    @Test
    void shouldKeepRoundIdAndPlayerId() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Deck deck = new Deck();
        Round round = new Round(roundId, playerId, deck);

        assertEquals(roundId, round.getRoundId());
        assertEquals(playerId, round.getPlayerId());
    }

    @Test
    void shouldRejectNullIdentifiers() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Deck deck = new Deck();

        assertThrows(NullPointerException.class, () -> new Round(null, playerId, deck));
        assertThrows(NullPointerException.class, () -> new Round(roundId, null, deck));
    }

    @Test
    void shouldRestoreRoundWithExistingState() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        List<Card> playerCards = List.of(new Card(Suit.HEARTS, Rank.AS), new Card(Suit.SPADES, Rank.KING));

        List<Card> dealerCards = List.of(new Card(Suit.CLUBS, Rank.NINE));

        Deck remainingDeck = new Deck(List.of(new Card(Suit.DIAMONDS, Rank.FIVE)));

        Round round = Round.restore(roundId, playerId, playerCards, dealerCards, remainingDeck, RoundStatus.PLAYER_TURN,
                null);

        assertEquals(roundId, round.getRoundId());
        assertEquals(playerId, round.getPlayerId());
        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());
        assertNull(round.getResult());
        assertEquals(2, round.getPlayerHand().getCards().size());
        assertEquals(1, round.getDealerHand().getCards().size());
    }

    @Test
    void shouldContinuePlayingWithRemainingDeckAfterRestore() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        List<Card> playerCards = List.of(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.SPADES, Rank.SIX));
        List<Card> dealerCards = List.of(new Card(Suit.CLUBS, Rank.NINE), new Card(Suit.DIAMONDS, Rank.SEVEN));
        List<Card> remainingCards = List.of(new Card(Suit.HEARTS, Rank.THREE), new Card(Suit.SPADES, Rank.FIVE));
        Round round = Round.restore(roundId, playerId, playerCards, dealerCards, new Deck(remainingCards),
                RoundStatus.PLAYER_TURN, null);

        round.hit();

        assertEquals(RoundStatus.PLAYER_TURN, round.getStatus());
        assertEquals(3, round.getPlayerHand().getCards().size());
        assertEquals(19, round.getPlayerHand().calculateScore());

        Card drawnCard = round.getPlayerHand().getCards().get(2);

        assertEquals(Suit.HEARTS, drawnCard.getSuit());
        assertEquals(Rank.THREE, drawnCard.getRank());
    }

    @Test
    void shouldRejectFinishedRoundWithoutResult() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> Round.restore(roundId, playerId, List.of(new Card(Suit.HEARTS, Rank.TEN),
                        new Card(Suit.SPADES, Rank.EIGHT)), List.of(new Card(Suit.CLUBS, Rank.NINE),
                        new Card(Suit.DIAMONDS, Rank.SEVEN)), new Deck(List.of()), RoundStatus.FINISHED, null));
    }

    @Test
    void shouldRejectUnfinishedRoundWithResult() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                Round.restore(roundId, playerId, List.of(new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.SPADES, Rank.SIX)),
                        List.of(new Card(Suit.CLUBS, Rank.NINE), new Card(Suit.DIAMONDS, Rank.SEVEN)),
                        new Deck(List.of()), RoundStatus.PLAYER_TURN, RoundResult.PLAYER_WIN));

    }

    @Test
    void shouldReturnRemainingCardsWithoutAllowingModification() {
        Card firstCard = new Card(Suit.HEARTS, Rank.AS);
        Card secondCard = new Card(Suit.SPADES, Rank.KING);

        Deck deck = new Deck(List.of(firstCard, secondCard));

        Round round = new Round(UUID.randomUUID(), UUID.randomUUID(), deck);

        List<Card> remainingCards = round.getRemainingCards();

        assertEquals(2, remainingCards.size());
        assertSame(firstCard, remainingCards.get(0));
        assertSame(secondCard, remainingCards.get(1));

        assertThrows(UnsupportedOperationException.class, () -> remainingCards.clear());

        assertEquals(2, round.getRemainingCards().size());
    }

}
