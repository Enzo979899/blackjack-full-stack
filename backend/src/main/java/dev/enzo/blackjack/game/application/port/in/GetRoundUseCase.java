package dev.enzo.blackjack.game.application.port.in;

import dev.enzo.blackjack.game.domain.model.Round;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetRoundUseCase {

    Mono<Round> getRound(UUID roundId);
}
