package dev.enzo.blackjack.player.application.port.in;

import dev.enzo.blackjack.player.application.dto.CreatePlayerResult;
import reactor.core.publisher.Mono;

public interface CreatePlayerUseCase {

    Mono<CreatePlayerResult> createPlayer(String name);

}
