package dev.enzo.blackjack.player.infrastructure.in.web;

import dev.enzo.blackjack.player.application.exception.DuplicatePlayerNameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = PlayerController.class)
public class PlayerExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPlayer(IllegalArgumentException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DuplicatePlayerNameException.class)
    public ResponseEntity<Map<String, String>> handleDuplicatePlayerName(DuplicatePlayerNameException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }
}
