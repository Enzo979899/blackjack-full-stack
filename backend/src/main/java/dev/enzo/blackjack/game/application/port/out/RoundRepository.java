package dev.enzo.blackjack.game.application.port.out;

import dev.enzo.blackjack.game.domain.model.Round;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface RoundRepository {

    Mono<Void> save(Round round);

    Mono<Round> findById(UUID roundId);

}
