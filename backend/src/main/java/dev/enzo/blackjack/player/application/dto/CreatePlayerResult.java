package dev.enzo.blackjack.player.application.dto;

import java.util.UUID;

public record CreatePlayerResult(UUID playerId, String name) {
}
