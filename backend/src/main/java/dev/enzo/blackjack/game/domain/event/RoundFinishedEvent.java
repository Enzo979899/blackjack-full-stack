package dev.enzo.blackjack.game.domain.event;

import dev.enzo.blackjack.game.domain.model.RoundResult;

import java.util.Objects;
import java.util.UUID;

public record RoundFinishedEvent(UUID roundId, UUID playerId, RoundResult result) {
    public RoundFinishedEvent {
        Objects.requireNonNull(roundId, "Round ID cannot be null");
        Objects.requireNonNull(playerId, "Player ID cannot be null");
        Objects.requireNonNull(result, "Round result cannot be null");
    }
}
