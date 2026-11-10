package dev.enzo.blackjack.game.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void shouldReturn52Cards() {
        Deck deck = new Deck();

        int remainingCards = deck.getRemainingCards();

        assertEquals(52, remainingCards);
    }

    @Test
    void shouldDecreaseRemainingCardWhenDrawingCards() {
        Deck deck = new Deck();

        Card drawCard = deck.cardDraw();

        assertNotNull(drawCard);
        assertEquals(51, deck.getRemainingCards());
    }

    @Test
    void shouldDraw52UniqueCards() {
        Deck deck = new Deck();
        Set<String> drawnCards = new HashSet<>();

        for (int i = 0; i < 52; i++) {
            Card card = deck.cardDraw();
            String identifier = card.getSuit() + "-" + card.getRank();

            drawnCards.add(identifier);
        }

        assertEquals(52, drawnCards.size());
        assertEquals(0, deck.getRemainingCards());
    }

    @Test
    void shouldThrowExceptionWhenDeckIsEmpty() {
        Deck deck = new Deck();

        for (int i = 0; i < 52; i++) {
            deck.cardDraw();
        }

        IllegalStateException exception = assertThrows(IllegalStateException.class, deck::cardDraw);

        assertEquals("The deck is empty", exception.getMessage());
    }

    @Test
    void shouldPreserveAllCardsAfterShuffle() {
        Deck deck = new Deck();

        deck.shuffle();

        Set<String> drawnCards = new HashSet<>();

        for (int i = 0; i < 52; i++) {
            Card card = deck.cardDraw();
            drawnCards.add(card.getSuit() + "-" + card.getRank());
        }

        assertEquals(52, drawnCards.size());
        assertEquals(0, deck.getRemainingCards());
    }

    @Test
    void shouldReturnImmutableCopyOfRemainingCards() {
        Deck deck = new Deck();

        List<Card> remainingCards = deck.getCards();

        assertEquals(52, remainingCards.size());
        assertThrows(UnsupportedOperationException.class, remainingCards::clear);

        Card firstCard = remainingCards.getFirst();

        assertEquals(firstCard, deck.cardDraw());
        assertEquals(51, deck.getRemainingCards());
        assertEquals(52, remainingCards.size());
    }

    @Test
    void shouldRestoreDeckWithRemainingCardsInOriginalOrder() {
        List<Card> remainingCards = List.of(new Card(Suit.HEARTS, Rank.AS), new Card(Suit.SPADES, Rank.KING));
        Deck deck = new Deck(remainingCards);

        assertEquals(2, deck.getRemainingCards());
        assertEquals(remainingCards.get(0), deck.cardDraw());
        assertEquals(remainingCards.get(1), deck.cardDraw());
        assertEquals(0, deck.getRemainingCards());
    }

    @Test
    void shouldRejectNullCardList() {
        assertThrows(NullPointerException.class, () -> new Deck(null));

    }

    @Test
    void shouldRejectListContainingNullCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Suit.HEARTS, Rank.AS));
        cards.add(null);

        assertThrows(IllegalArgumentException.class, () -> new Deck(cards));
    }

    @Test
    void shouldMakeDefensiveCopyOfProvidedCards() {
        List<Card> originalCards = new ArrayList<>();
        originalCards.add(new Card(Suit.HEARTS, Rank.AS));
        originalCards.add(new Card(Suit.SPADES, Rank.KING));

        Deck deck = new Deck(originalCards);

        originalCards.clear();

        assertEquals(2, deck.getRemainingCards());

        Card firstCard = deck.cardDraw();

        assertEquals(Suit.HEARTS, firstCard.getSuit());
        assertEquals(Rank.AS, firstCard.getRank());

        Card secondCard = deck.cardDraw();

        assertEquals(Suit.SPADES, secondCard.getSuit());
        assertEquals(Rank.KING, secondCard.getRank());
    }

}
