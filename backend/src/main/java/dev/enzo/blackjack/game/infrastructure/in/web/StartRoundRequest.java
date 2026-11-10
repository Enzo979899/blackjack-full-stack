package dev.enzo.blackjack.game.infrastructure.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartRoundRequest(@NotNull(message = "Player ID is required") UUID playerId) {

}
