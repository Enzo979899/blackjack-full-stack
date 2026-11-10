package dev.enzo.blackjack.player.infrastructure.in.web;

import dev.enzo.blackjack.player.application.dto.CreatePlayerResult;
import dev.enzo.blackjack.player.application.port.in.CreatePlayerUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final CreatePlayerUseCase createPlayerUseCase;

    public PlayerController(CreatePlayerUseCase createPlayerUseCase) {
        this.createPlayerUseCase = createPlayerUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreatePlayerResult> createPlayer(
            @Valid @RequestBody CreatePlayerRequest request) {

        return createPlayerUseCase.createPlayer(request.name());
    }


}

