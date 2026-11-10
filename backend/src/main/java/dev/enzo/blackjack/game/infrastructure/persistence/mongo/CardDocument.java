package dev.enzo.blackjack.game.infrastructure.persistence.mongo;

import dev.enzo.blackjack.game.domain.model.Rank;
import dev.enzo.blackjack.game.domain.model.Suit;

public record CardDocument(Suit suit, Rank rank) {

}
