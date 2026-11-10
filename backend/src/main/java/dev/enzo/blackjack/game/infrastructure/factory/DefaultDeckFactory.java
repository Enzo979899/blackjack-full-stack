
package dev.enzo.blackjack.game.infrastructure.factory;

import dev.enzo.blackjack.game.application.port.out.DeckFactory;
import dev.enzo.blackjack.game.domain.model.Deck;

public class DefaultDeckFactory implements DeckFactory {

    @Override
    public Deck createDeck() {
        Deck deck = new Deck();
        deck.shuffle();
        return deck;
    }
}
