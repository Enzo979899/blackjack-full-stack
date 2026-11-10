package dev.enzo.blackjack.game.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public Deck(List<Card> remainingCards) {
        Objects.requireNonNull(remainingCards, "Cards cannot be null");

        if (remainingCards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Cards cannot contain null");
        }

        this.cards = new ArrayList<>(remainingCards);
    }

    public int getRemainingCards() {
        return cards.size();
    }

    public Card cardDraw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("The deck is empty");
        }
        return cards.removeFirst();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> getCards() {
        return List.copyOf(cards);
    }
}
