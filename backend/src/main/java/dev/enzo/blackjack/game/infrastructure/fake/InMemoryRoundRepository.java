package dev.enzo.blackjack.game.infrastructure.fake;

import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.model.Round;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoundRepository implements RoundRepository {

    private final Map<UUID, Round> rounds = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> save(Round round) {
        return Mono.fromRunnable(() -> rounds.put(round.getRoundId(), round));
    }

    @Override
    public Mono<Round> findById(UUID roundId) {
        return Mono.defer(() -> Mono.justOrEmpty(rounds.get(roundId)));
    }
}
