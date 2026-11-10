package dev.enzo.blackjack.game.domain.model;


import dev.enzo.blackjack.game.domain.exception.InvalidRoundStateException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Round {
    private final UUID roundId;
    private final UUID playerId;
    private final Hand playerHand;
    private final Hand dealerHand;
    private final Deck deck;
    private RoundStatus status;
    private RoundResult result;

    public Round(UUID roundId, UUID playerId, Deck deck) {
        this.roundId = Objects.requireNonNull(roundId, "Round ID cannot be null");
        this.playerId= Objects.requireNonNull(playerId, "Player ID cannot be null");
        this.deck = Objects.requireNonNull(deck, "The deck cannot be null");
        this.playerHand = new Hand();
        this.dealerHand = new Hand();
        this.status = RoundStatus.WAITING_TO_START;
        this.result = null;

    }

    private Round(UUID roundId, UUID playerId, List<Card> playerCards, List<Card> dealerCards, Deck deck,
                  RoundStatus status, RoundResult result) {
        this.roundId = Objects.requireNonNull(roundId);
        this.playerId = Objects.requireNonNull(playerId);
        this.playerHand = new Hand(Objects.requireNonNull(playerCards));
        this.dealerHand = new Hand(Objects.requireNonNull(dealerCards));
        this.deck = Objects.requireNonNull(deck);
        this.status = Objects.requireNonNull(status);
        this.result = result;
    }

    public static Round restore(UUID roundId, UUID playerId, List<Card> playerCards, List<Card> dealerCards, Deck deck,
                                RoundStatus status, RoundResult result) {

        if (status == RoundStatus.FINISHED && result == null) {
            throw new IllegalArgumentException("A finished round must have a result");
        }

        if (status != null && status != RoundStatus.FINISHED && result != null) {
            throw new IllegalArgumentException("An unfinished round cannot have a result");
        }
        return new Round(roundId, playerId, playerCards, dealerCards, deck, status, result);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public HandView getPlayerHand() {
        return new HandSnapshot(this.playerHand.getCards());
    }

    public HandView getDealerHand() {
        return new HandSnapshot(this.dealerHand.getCards());
    }

    public RoundStatus getStatus() {
        return status;
    }

    public RoundResult getResult() {
        return result;
    }

    public List<Card> getRemainingCards() {
        return deck.getCards();
    }

    public void start() {
        if (this.status != RoundStatus.WAITING_TO_START) {
            throw new InvalidRoundStateException("Round is already started!");
        }
        this.playerHand.addCard(this.deck.cardDraw());
        this.dealerHand.addCard(this.deck.cardDraw());
        this.playerHand.addCard(this.deck.cardDraw());
        this.dealerHand.addCard(this.deck.cardDraw());

        if (this.playerHand.isBlackJack() && this.dealerHand.isBlackJack()) {
            this.result = RoundResult.PUSH;
        } else if (this.playerHand.isBlackJack()) {
            this.result = RoundResult.PLAYER_WIN;
        } else if (this.dealerHand.isBlackJack()) {
            this.result = RoundResult.DEALER_WIN;
        }

        if (this.result != null) {
            this.status = RoundStatus.FINISHED;
            return;
        }
        this.status = RoundStatus.PLAYER_TURN;
    }

    public void hit() {
        if (this.status != RoundStatus.PLAYER_TURN) {
            throw new InvalidRoundStateException("It's not the player's turn!");
        }

        if (this.playerHand.calculateScore() == 21) {
            throw new InvalidRoundStateException("Cannot hit with 21");
        }
        this.playerHand.addCard(this.deck.cardDraw());

        if  (this.playerHand.isBusted()) {
            this.status = RoundStatus.FINISHED;
            this.result = RoundResult.DEALER_WIN;
        }
    }

    public void stand() {
        if (this.status != RoundStatus.PLAYER_TURN) {
            throw new InvalidRoundStateException("It's not the player's turn!");
        }
        this.status = RoundStatus.DEALER_TURN;
    }

    public void playDealerTurn() {
        if (this.status != RoundStatus.DEALER_TURN) {
            throw new InvalidRoundStateException("It's not the dealer's turn!");
        }

        while (this.dealerHand.calculateScore() < 17) {
            this.dealerHand.addCard(this.deck.cardDraw());
        }

        int playerScore = this.playerHand.calculateScore();
        int dealerScore = this.dealerHand.calculateScore();

        if (this.dealerHand.isBusted()) {
            this.result = RoundResult.PLAYER_WIN;
        } else if (playerScore > dealerScore) {
            this.result = RoundResult.PLAYER_WIN;
        } else if (dealerScore > playerScore) {
            this.result = RoundResult.DEALER_WIN;
        } else {
            this.result = RoundResult.PUSH;
        }

        this.status = RoundStatus.FINISHED;
    }

}
