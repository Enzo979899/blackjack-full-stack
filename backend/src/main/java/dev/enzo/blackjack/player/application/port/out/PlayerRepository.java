package dev.enzo.blackjack.player.application.port.out;

import dev.enzo.blackjack.player.domain.model.Player;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PlayerRepository {

    Mono<Void> save(Player player);

    Mono<Player> findById(UUID playerId);

    Flux<Player> findAll();
}
