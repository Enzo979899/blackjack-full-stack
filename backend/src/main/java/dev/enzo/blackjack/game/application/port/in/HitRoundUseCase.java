package dev.enzo.blackjack.game.application.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface HitRoundUseCase {

    Mono<Void> hit(UUID roundId);

}
