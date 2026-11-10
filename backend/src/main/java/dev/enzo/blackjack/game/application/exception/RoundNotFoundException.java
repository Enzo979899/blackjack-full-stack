package dev.enzo.blackjack.game.application.exception;

import java.util.UUID;

public class RoundNotFoundException extends RuntimeException {

    public RoundNotFoundException(UUID roundId) {
        super("Round not found: " + roundId);
    }
}
