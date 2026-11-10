package dev.enzo.blackjack.game.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hand {
    private final List<Card> cards;

    public Hand() {
        this(List.of());
    }

    Hand(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public void addCard(Card card) {
        cards.add(Objects.requireNonNull(card, "Card cannot be null."));
    }

    public int getCardCount() {
        return cards.size();
    }

    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    public int calculateScore() {
        int score = 0;
        int ases = 0;

        for (Card card : cards) {
            score += card.getValue();

            if (card.getRank() == Rank.AS) {
                ases++;
            }
        }
        while (score > 21 && ases > 0) {
            score -= 10;
            ases--;
        }

        return score;
    }

    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public boolean isBlackJack() {
        return cards.size() == 2 && calculateScore() == 21;
    }
}
