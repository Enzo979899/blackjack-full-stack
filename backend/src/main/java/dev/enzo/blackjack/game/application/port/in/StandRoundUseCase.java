package dev.enzo.blackjack.game.application.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StandRoundUseCase {

    Mono<Void> stand(UUID roundId);

}
