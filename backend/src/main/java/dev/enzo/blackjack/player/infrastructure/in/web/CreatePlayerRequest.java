package dev.enzo.blackjack.player.infrastructure.in.web;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(@NotBlank(message = "Player name cannot be blank") String name) {

}
