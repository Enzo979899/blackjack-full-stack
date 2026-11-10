package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SpringDataPlayerRepository
        extends ReactiveCrudRepository<PlayerEntity, String> {
}
