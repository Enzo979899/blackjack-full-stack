package dev.enzo.blackjack.player.infrastructure.fake;

import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.domain.model.Player;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPlayerRepository implements PlayerRepository {

    private final Map<UUID, Player> players = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> save(Player player) {
        return Mono.fromRunnable(() -> players.put(player.getId(), player));
    }

    @Override
    public Mono<Player> findById(UUID playerId) {
        return Mono.defer(() -> Mono.justOrEmpty(players.get(playerId)));
    }

    @Override
    public Flux<Player> findAll() {
        return Flux.defer(() -> Flux.fromIterable(players.values()));
    }

}
