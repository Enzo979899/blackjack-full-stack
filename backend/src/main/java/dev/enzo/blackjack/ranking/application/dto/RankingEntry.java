package dev.enzo.blackjack.ranking.application.dto;

import java.util.Objects;
import java.util.UUID;

public record RankingEntry(UUID playerId, String playerName, int wins, int losses, int draws) {
    public RankingEntry {
        Objects.requireNonNull(playerId, "Player ID cannot be null");
        Objects.requireNonNull(playerName, "Player name cannot be null");

        if (playerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }

        if (wins < 0 || losses < 0 || draws < 0) {
            throw new IllegalArgumentException("Statistics cannot be negative");
        }
    }

}
