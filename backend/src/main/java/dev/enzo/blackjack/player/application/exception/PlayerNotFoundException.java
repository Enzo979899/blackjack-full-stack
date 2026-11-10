package dev.enzo.blackjack.player.application.exception;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(UUID playerId) {
        super("Player not found: " + playerId);
    }
}
