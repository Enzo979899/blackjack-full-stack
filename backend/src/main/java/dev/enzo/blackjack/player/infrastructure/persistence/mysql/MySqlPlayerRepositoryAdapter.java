package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.domain.model.Player;
import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@Profile("mysql")
public class MySqlPlayerRepositoryAdapter implements PlayerRepository {
    private final SpringDataPlayerRepository repository;
    private final DatabaseClient databaseClient;

    public MySqlPlayerRepositoryAdapter(SpringDataPlayerRepository repository, DatabaseClient databaseClient) {
        this.repository = repository;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> save(Player player) {
        return Mono.defer(() -> {
            PlayerEntity entity = PlayerEntityMapper.toEntity(player);

            return databaseClient.sql("""
                            INSERT INTO players (id, name, wins, losses, draws)
                            VALUES (:id, :name, :wins, :losses, :draws)
                            ON DUPLICATE KEY UPDATE
                                name = VALUES(name)
                            """).bind("id", entity.getId()).bind("name", entity.getName())
                    .bind("wins", entity.getWins()).bind("losses", entity.getLosses())
                    .bind("draws", entity.getDraws()).fetch().rowsUpdated().then();
        });
    }

    @Override
    public Mono<Player> findById(UUID playerId) {
        return repository.findById(playerId.toString()).map(PlayerEntityMapper::toDomain);
    }

    @Override
    public Flux<Player> findAll() {
        return repository.findAll().map(PlayerEntityMapper::toDomain);
    }

}
