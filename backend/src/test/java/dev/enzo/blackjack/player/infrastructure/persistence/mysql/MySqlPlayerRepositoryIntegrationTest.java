package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import dev.enzo.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration",
        "spring.r2dbc.url=r2dbc:mysql://localhost:3307/blackjack_test"
})

@ActiveProfiles("mysql")
class MySqlPlayerRepositoryIntegrationTest {

    @Autowired
    private MySqlPlayerRepositoryAdapter adapter;

    @Autowired
    private SpringDataPlayerRepository springRepository;

    private final List<UUID> createdPlayerIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        StepVerifier.create(reactor.core.publisher.Flux.fromIterable(createdPlayerIds)
                .concatMap(id -> springRepository.deleteById(id.toString()))).verifyComplete();

        createdPlayerIds.clear();

    }

    @Test
    void shouldSaveAndFindPlayerInMySQL() {
        UUID playerId = UUID.randomUUID();
        Player player = new Player(playerId, "TestPlayer");

        createdPlayerIds.add(playerId);

        StepVerifier.create(adapter.save(player)).verifyComplete();

        StepVerifier.create(adapter.findById(playerId)).assertNext(savedPlayer -> {
            assertEquals(playerId, savedPlayer.getId());
            assertEquals("TestPlayer", savedPlayer.getName());
            assertEquals(0, savedPlayer.getWins());
            assertEquals(0, savedPlayer.getLosses());
            assertEquals(0, savedPlayer.getDraws());
        }).verifyComplete();
    }

    @Test
    void shouldNotOverwriteExistingPlayerStatistics() {
        UUID playerId = UUID.randomUUID();
        Player originalPlayer = new Player(playerId, "TestPlayer", 5, 3, 2);

        createdPlayerIds.add(playerId);

        StepVerifier.create(adapter.save(originalPlayer)).verifyComplete();

        Player updatedPlayer = new Player(playerId, "UpdatedPlayer", 0, 0, 0);

        StepVerifier.create(adapter.save(updatedPlayer)).verifyComplete();

        StepVerifier.create(adapter.findById(playerId)).assertNext(savedPlayer -> {
            assertEquals(playerId, savedPlayer.getId());
            assertEquals("UpdatedPlayer", savedPlayer.getName());
            assertEquals(5, savedPlayer.getWins());
            assertEquals(3, savedPlayer.getLosses());
            assertEquals(2, savedPlayer.getDraws());
        }).verifyComplete();
    }

}
