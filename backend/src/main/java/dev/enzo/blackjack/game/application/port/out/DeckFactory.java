
package dev.enzo.blackjack.game.application.port.out;

import dev.enzo.blackjack.game.domain.model.Deck;

public interface DeckFactory {

    Deck createDeck();

}
