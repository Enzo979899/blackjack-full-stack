package dev.enzo.blackjack.game.infrastructure.persistence.mongo;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.game.domain.model.RoundStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "rounds")
public class RoundDocument {

    @Id
    private String id;
    private String playerId;
    private List<CardDocument> playerCards;
    private List<CardDocument> dealerCards;
    private List<CardDocument> remainingCards;
    private RoundStatus status;
    private RoundResult result;

    public RoundDocument(String id, String playerId, List<CardDocument> playerCards, List<CardDocument> dealerCards,
                         List<CardDocument> remainingCards, RoundStatus status, RoundResult result) {
        this.id = id;
        this.playerId = playerId;
        this.playerCards = playerCards;
        this.dealerCards = dealerCards;
        this.remainingCards = remainingCards;
        this.status = status;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public List<CardDocument> getPlayerCards() {
        return playerCards;
    }

    public List<CardDocument> getDealerCards() {
        return dealerCards;
    }

    public List<CardDocument> getRemainingCards() {
        return remainingCards;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public RoundResult getResult() {
        return result;
    }
}
