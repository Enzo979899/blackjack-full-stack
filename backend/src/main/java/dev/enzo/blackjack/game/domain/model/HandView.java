package dev.enzo.blackjack.game.domain.model;

import java.util.List;

public interface HandView {

    int getCardCount();

    List<Card> getCards();

    int calculateScore();

    boolean isBusted();

    boolean isBlackJack();
}
