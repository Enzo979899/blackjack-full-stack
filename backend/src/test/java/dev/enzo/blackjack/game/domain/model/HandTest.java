package dev.enzo.blackjack.game.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandTest {

    @Test
    void shuldStartWithEmptyHand() {
        Hand hand = new Hand();

        int cardCount = hand.getCardCount();

        assertEquals(0, cardCount);
    }

    @Test
    void shouldAddOneCardToHand() {
        Hand hand = new Hand();

        Card card = new Card(Suit.HEARTS, Rank.FIVE);

        hand.addCard(card);

        assertEquals(1, hand.getCardCount());
    }

    @Test
    void shouldRejectNullCards() {
        Hand hand = new Hand();

        assertThrows(NullPointerException.class, () -> hand.addCard(null));

        assertEquals(0, hand.getCardCount());
    }

    @Test
    void shouldReturnCardsInHand() {
        Hand hand = new Hand();

        Card as = new Card(Suit.DIAMONDS, Rank.AS);
        Card five = new Card(Suit.HEARTS, Rank.FIVE);

        hand.addCard(as);
        hand.addCard(five);

        assertEquals(2, hand.getCardCount());
        assertSame(as, hand.getCards().getFirst());
        assertSame(five, hand.getCards().getLast());
    }

    @Test
    void shouldNotAllowModifyingCardsFromOutside() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.HEARTS, Rank.SIX));

        assertThrows(UnsupportedOperationException.class, () -> hand.getCards().clear());

        assertEquals(1, hand.getCardCount());
    }

    @Test
    void shouldAddMultipleCardsToHand() {
        Hand hand = new Hand();

        Card card1 = new Card(Suit.CLUBS, Rank.JACK);
        Card card2 = new Card(Suit.HEARTS, Rank.EIGHT);

        hand.addCard(card1);
        hand.addCard(card2);

        assertEquals(2, hand.getCardCount());
    }

    @Test
    void shouldReturnZeroWhenHandIsEmpty() {
        Hand hand = new Hand();

        assertEquals(0, hand.getCardCount());
        assertEquals(0, hand.calculateScore());
    }

    @Test
    void shouldCalculateScoreWithCards() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.CLUBS, Rank.EIGHT));
        hand.addCard(new Card(Suit.HEARTS, Rank.JACK));

        assertEquals(18, hand.calculateScore());
    }

    @Test
    void shouldAdjustOnlyOneAsWhenNecessary() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.HEARTS, Rank.AS));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.AS));
        hand.addCard(new Card(Suit.HEARTS, Rank.NINE));

        assertEquals(21, hand.calculateScore());
    }

    @Test
    void shouldAdjustMultipleAsesWhenNecessary() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.HEARTS, Rank.AS));
        hand.addCard(new Card(Suit.SPADES, Rank.AS));
        hand.addCard(new Card(Suit.SPADES, Rank.FIVE));
        hand.addCard(new Card(Suit.SPADES, Rank.NINE));

        assertEquals(16, hand.calculateScore());
    }

    @Test
    void shouldBeBustedWhenScoreExceeds21() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.SPADES,Rank.KING));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));
        hand.addCard(new Card(Suit.CLUBS, Rank.FIVE));

        assertTrue(hand.isBusted());
    }

    @Test
    void shouldNotBeBustedWhenScoreIs21() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.CLUBS, Rank.KING));
        hand.addCard(new Card(Suit.SPADES, Rank.AS));

        assertFalse(hand.isBusted());
    }

    @Test
    void shouldNotBeBustedWhenAsCanCountAsOne() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.CLUBS, Rank.AS));
        hand.addCard(new Card(Suit.SPADES, Rank.EIGHT));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        assertFalse(hand.isBusted());
        assertEquals(14, hand.calculateScore());
    }

    @Test
    void shouldBeBlackJackWithAsAndKing() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.DIAMONDS, Rank.AS));
        hand.addCard(new Card(Suit.SPADES, Rank.KING));

        assertTrue(hand.isBlackJack());
    }

    @Test
    void shouldNotBeBlackJackWithThreeCardsScoreIs21() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.HEARTS, Rank.EIGHT));
        hand.addCard(new Card(Suit.HEARTS, Rank.SEVEN));
        hand.addCard(new Card(Suit.HEARTS, Rank.SIX));

        assertFalse(hand.isBlackJack());
        assertEquals(21, hand.calculateScore());
    }

    @Test
    void shouldNotBeBlackJackWithTwoCardsBelow21() {
        Hand hand = new Hand();

        hand.addCard(new Card(Suit.DIAMONDS, Rank.NINE));
        hand.addCard(new Card(Suit.HEARTS, Rank.EIGHT));

        assertFalse(hand.isBlackJack());
    }
}

