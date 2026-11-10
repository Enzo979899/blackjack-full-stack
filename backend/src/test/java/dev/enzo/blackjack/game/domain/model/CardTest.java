package dev.enzo.blackjack.game.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void shouldReturnElevenWhenCardIsAce() {
        Card card = new Card(Suit.HEARTS, Rank.AS);

        int value = card.getValue();

        assertEquals(11, value);
    }

    @Test
    void shouldReturnNumericValueWhenCardIsNumber() {
        Card card = new Card(Suit.SPADES, Rank.SEVEN);

        int value = card.getValue();

        assertEquals(7, value);
    }

    @Test
    void shouldReturnTenWhenCardIsFaceCard() {
        Card jack = new Card(Suit.CLUBS, Rank.JACK);
        Card queen = new Card(Suit.DIAMONDS, Rank.QUEEN);
        Card king = new Card(Suit.HEARTS, Rank.KING);

        assertAll(() -> assertEquals(10, jack.getValue()), () -> assertEquals(10, queen.getValue()),
                () -> assertEquals(10, king.getValue()));
    }

    @Test
    void shouldReturnCorrectSuitAndRank() {
        Card card = new Card(Suit.DIAMONDS, Rank.FIVE);

        assertEquals(Suit.DIAMONDS, card.getSuit());
        assertEquals(Rank.FIVE, card.getRank());
    }

    @Test
    void shouldThrowExceptionWhenSuitIsNull() {
        assertThrows(NullPointerException.class, () -> new Card(null, Rank.AS));
    }

    @Test
    void shouldThrowExceptionWhenRankIsNull() {
        assertThrows(NullPointerException.class, () -> new Card(Suit.HEARTS, null));
    }
}
