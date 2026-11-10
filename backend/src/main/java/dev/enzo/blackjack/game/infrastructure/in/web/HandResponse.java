package dev.enzo.blackjack.game.infrastructure.in.web;

import dev.enzo.blackjack.game.domain.model.Card;
import dev.enzo.blackjack.game.domain.model.HandSnapshot;
import dev.enzo.blackjack.game.domain.model.HandView;

import java.util.List;

public record HandResponse(List<CardResponse> cards, int score) {

    public HandResponse {
        cards = List.copyOf(cards);
    }

    public static HandResponse from(HandView hand) {
        return new HandResponse(hand.getCards().stream().map(CardResponse::from).toList(), hand.calculateScore());
    }

    public static HandResponse fromVisibleCards(List<Card> visibleCards) {
        return from(new HandSnapshot(visibleCards));
    }
}
