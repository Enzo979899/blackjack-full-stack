package dev.enzo.blackjack.game.infrastructure.factory;

import dev.enzo.blackjack.game.domain.model.Deck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDeckFactoryTest {

    @Test
    void shouldCreateDeckWith52Cards() {
        DefaultDeckFactory factory = new DefaultDeckFactory();
        Deck deck = factory.createDeck();

        assertNotNull(deck);
        assertEquals(52, deck.getRemainingCards());
    }

    @Test
    void shouldCreateIndependentDecks() {
        DefaultDeckFactory factory = new DefaultDeckFactory();
        Deck firstDeck = factory.createDeck();
        Deck secondDeck = factory.createDeck();

        assertNotSame(firstDeck, secondDeck);
        assertEquals(52, firstDeck.getRemainingCards());
        assertEquals(52, secondDeck.getRemainingCards());

        firstDeck.cardDraw();

        assertEquals(51, firstDeck.getRemainingCards());
        assertEquals(52, secondDeck.getRemainingCards());
    }
}
