package dev.enzo.blackjack.game.infrastructure.in.web;

import dev.enzo.blackjack.game.application.exception.RoundNotFoundException;
import dev.enzo.blackjack.game.domain.exception.InvalidRoundStateException;
import dev.enzo.blackjack.player.application.exception.PlayerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.Map;

@RestControllerAdvice(assignableTypes = RoundController.class)
public class RoundExceptionHandler {

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePlayerNotFound(PlayerNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(RoundNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoundNotFound(RoundNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(InvalidRoundStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRoundState(InvalidRoundStateException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }
}
