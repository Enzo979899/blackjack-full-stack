package dev.enzo.blackjack.game.domain.model;

import java.util.Objects;

public final class Card {
    private final Suit suit;
    private final Rank rank;

    public Card (Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit, "Suit cannot be null");
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.getValue();
    }

}
