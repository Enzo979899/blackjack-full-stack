package dev.enzo.blackjack.game.domain.exception;

public class InvalidRoundStateException extends IllegalStateException {

    public InvalidRoundStateException(String message) {
        super(message);
    }
}
