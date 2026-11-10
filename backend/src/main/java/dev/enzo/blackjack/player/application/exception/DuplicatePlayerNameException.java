package dev.enzo.blackjack.player.application.exception;

public class DuplicatePlayerNameException extends RuntimeException {

    public DuplicatePlayerNameException() {
        super("Player name already exists");
    }
}
