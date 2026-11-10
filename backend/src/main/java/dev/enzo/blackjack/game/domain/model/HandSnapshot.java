package dev.enzo.blackjack.game.domain.model;

import java.util.List;

public record HandSnapshot(List<Card> cards) implements HandView {

    public HandSnapshot {
        cards = List.copyOf(cards);
    }

    @Override
    public int getCardCount() {
        return cards.size();
    }

    @Override
    public List<Card> getCards() {
        return cards;
    }

    @Override
    public int calculateScore() {
        return new Hand(cards).calculateScore();
    }

    @Override
    public boolean isBusted() {
        return new Hand(cards).isBusted();
    }

    @Override
    public boolean isBlackJack() {
        return new Hand(cards).isBlackJack();
    }
}
