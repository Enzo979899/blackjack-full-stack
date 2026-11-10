package dev.enzo.blackjack.game.application.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StartRoundUseCase {

    Mono<UUID> startRound(UUID playerId);

}
