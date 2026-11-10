package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.player.application.port.out.RoundResultRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Repository
@Profile("mysql")
public class MySqlRoundResultRepositoryAdapter implements RoundResultRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    public MySqlRoundResultRepositoryAdapter(DatabaseClient databaseClient,
                                             TransactionalOperator transactionalOperator) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
        this.transactionalOperator = Objects.requireNonNull(transactionalOperator);
    }

    @Override
    public Mono<Boolean> recordIfAbsent(UUID roundId, UUID playerId, RoundResult result) {
        return Mono.defer(() -> {
            Objects.requireNonNull(roundId, "Round ID cannot be null");
            Objects.requireNonNull(playerId, "Player ID cannot be null");
            Objects.requireNonNull(result, "Round result cannot be null");

            String column = switch (result) {
                case PLAYER_WIN -> "wins";
                case DEALER_WIN -> "losses";
                case PUSH -> "draws";
            };

            return databaseClient.sql("""
                            INSERT INTO round_results
                                (round_id, player_id, result)
                            VALUES (:roundId, :playerId, :result)
                            """)
                    .bind("roundId", roundId.toString()).bind("playerId", playerId.toString())
                    .bind("result", result.name()).fetch().rowsUpdated()
                    .onErrorResume(DuplicateKeyException.class, exception -> Mono.just(0L))
                    .flatMap(insertedRows -> {
                        if (insertedRows == 0) {
                            return Mono.just(false);
                        }

                        return databaseClient.sql(
                                        "UPDATE players SET " + column +
                                        " = " + column + " + 1 WHERE id = :playerId")
                                .bind("playerId", playerId.
                                        toString()).fetch().rowsUpdated().flatMap(updatedRows -> {
                                            if (updatedRows == 0) {
                                                return Mono.error(new IllegalArgumentException("Player not found: " +
                                                        playerId));
                                    }

                                    return Mono.just(true);
                                });
                    }).as(transactionalOperator::transactional);

        });
    }

}
