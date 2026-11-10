package dev.enzo.blackjack.player.application.port.out;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoundResultRepository {

    Mono<Boolean> recordIfAbsent(UUID roundId, UUID playerId, RoundResult result);

}
