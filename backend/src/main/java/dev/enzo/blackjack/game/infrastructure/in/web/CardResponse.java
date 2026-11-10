package dev.enzo.blackjack.game.infrastructure.in.web;

import dev.enzo.blackjack.game.domain.model.Card;
import dev.enzo.blackjack.game.domain.model.Rank;
import dev.enzo.blackjack.game.domain.model.Suit;

public record CardResponse(Suit suit, Rank rank) {

    public static CardResponse from(Card card) {
        return new CardResponse(card.getSuit(), card.getRank());
    }
}
