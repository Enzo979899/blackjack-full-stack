package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration",
        "spring.r2dbc.url=r2dbc:mysql://localhost:3307/blackjack_test"
})

@ActiveProfiles("mysql")
class MySqlRoundResultRepositoryIntegrationTest {

    @Autowired
    private MySqlRoundResultRepositoryAdapter resultAdapter;

    @Autowired
    private MySqlPlayerRepositoryAdapter playerAdapter;

    @Autowired
    private DatabaseClient databaseClient;

    private UUID playerId;

    @AfterEach
    void cleanUp() {
        if (playerId == null) {
            return;
        }

        StepVerifier.create(
                databaseClient.sql("DELETE FROM round_results WHERE player_id = :playerId")
                        .bind("playerId", playerId.toString()).fetch().rowsUpdated()
                        .then(databaseClient.sql("DELETE FROM players WHERE id = :playerId").bind("playerId",
                                playerId.toString()).fetch().rowsUpdated())).expectNextCount(1).verifyComplete();
    }

    @Test
    void shouldIncrementWinsWhenPlayerWins() {
        playerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        Player player = new Player(playerId, "TestPlayer");

        StepVerifier.create(playerAdapter.save(player)).verifyComplete();

        StepVerifier.create(resultAdapter.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN)).expectNext(true)
                .verifyComplete();

        StepVerifier.create(playerAdapter.findById(playerId)).assertNext(savedPlayer -> {
            assertEquals(1, savedPlayer.getWins());
            assertEquals(0, savedPlayer.getLosses());
            assertEquals(0, savedPlayer.getDraws());
        }).verifyComplete();

    }

    @Test
    void shouldIncrementLossesWhenDealerWins() {
        playerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(
                playerAdapter.save(new Player(playerId, "TestPlayer"))
        ).verifyComplete();

        StepVerifier.create(
                resultAdapter.recordIfAbsent(roundId, playerId, RoundResult.DEALER_WIN)
        ).expectNext(true).verifyComplete();

        StepVerifier.create(playerAdapter.findById(playerId))
                .assertNext(player -> {
                    assertEquals(0, player.getWins());
                    assertEquals(1, player.getLosses());
                    assertEquals(0, player.getDraws());
                }).verifyComplete();
    }

    @Test
    void shouldIncrementDrawsWhenRoundIsPush() {
        playerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(
                playerAdapter.save(new Player(playerId, "TestPlayer"))
        ).verifyComplete();

        StepVerifier.create(
                resultAdapter.recordIfAbsent(roundId, playerId, RoundResult.PUSH)
        ).expectNext(true).verifyComplete();

        StepVerifier.create(playerAdapter.findById(playerId))
                .assertNext(player -> {
                    assertEquals(0, player.getWins());
                    assertEquals(0, player.getLosses());
                    assertEquals(1, player.getDraws());
                }).verifyComplete();
    }

    @Test
    void shouldNotCountSameRoundTwice() {
        playerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(playerAdapter.save(new Player(playerId, "TestPlayer"))
        ).verifyComplete();

        StepVerifier.create(resultAdapter.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN))
                .expectNext(true).verifyComplete();

        StepVerifier.create(resultAdapter.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN))
                .expectNext(false).verifyComplete();

        StepVerifier.create(playerAdapter.findById(playerId)).assertNext(player -> {
            assertEquals(1, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getDraws());
        }).verifyComplete();
    }

    @Test
    void shouldRollbackWhenPlayerDoesNotExist() {
        UUID nonExistingPlayerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(resultAdapter.recordIfAbsent(roundId, nonExistingPlayerId, RoundResult.PLAYER_WIN))
                .expectError().verify();

        StepVerifier.create(databaseClient.sql("""
                   SELECT COUNT(*) AS total
                   FROM round_results
                   WHERE round_id = :roundId
                   """).bind("roundId", roundId.toString()).map((row, metadata) ->
                                row.get("total", Long.class)).one()).expectNext(0L).verifyComplete();
    }

    @Test
    void shouldRollbackInsertWhenStatisticsUpdateFails() {
        playerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(playerAdapter.save(
                new Player(playerId, "RollbackTest", Integer.MAX_VALUE, 0, 0))).verifyComplete();

        StepVerifier.create(resultAdapter.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN)).expectError()
                .verify();

        StepVerifier.create(databaseClient.sql("""
                    SELECT COUNT(*) AS total
                    FROM round_results
                    WHERE round_id = :roundId
                    """)
                        .bind("roundId", roundId.toString()).map((row, metadata) ->
                                row.get("total", Long.class)).one()).expectNext(0L).verifyComplete();

        StepVerifier.create(playerAdapter.findById(playerId)).assertNext(player ->
                        assertEquals(Integer.MAX_VALUE, player.getWins())).verifyComplete();
    }

}
