package dev.enzo.blackjack.player.infrastructure.fake;

import dev.enzo.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPlayerRepositoryTest {

    @Test
    void shouldSaveAndFindPlayerById() {
        InMemoryPlayerRepository repository = new InMemoryPlayerRepository();
        UUID playerId = UUID.randomUUID();
        Player player = new Player(playerId, "Carla");

        StepVerifier.create(repository.save(player)).verifyComplete();

        StepVerifier.create(repository.findById(playerId))
                .assertNext(savedPlayer -> assertSame(player, savedPlayer)).verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenPlayerDoesNotExist() {
        InMemoryPlayerRepository repository = new InMemoryPlayerRepository();
        UUID playerId = UUID.randomUUID();

        StepVerifier.create(repository.findById(playerId)).verifyComplete();
    }

    @Test
    void shouldStoreMultiplePlayersIndependently() {
        InMemoryPlayerRepository repository = new InMemoryPlayerRepository();

        UUID firstPlayerId = UUID.randomUUID();
        Player firstPlayer = new Player(firstPlayerId, "Enzo");
        UUID secondPlayerId = UUID.randomUUID();
        Player secondPlayer = new Player(secondPlayerId, "Carla");

        StepVerifier.create(repository.save(firstPlayer)).verifyComplete();

        StepVerifier.create(repository.save(secondPlayer)).verifyComplete();

        StepVerifier.create(repository.findById(firstPlayerId))
                .assertNext(savedPlayer -> assertSame(firstPlayer, savedPlayer)).verifyComplete();

        StepVerifier.create(repository.findById(secondPlayerId))
                .assertNext(savedPlayer -> assertSame(secondPlayer, savedPlayer)).verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenNoPlayersExist() {
        InMemoryPlayerRepository repository = new InMemoryPlayerRepository();

        StepVerifier.create(repository.findAll()).verifyComplete();
    }

    @Test
    void shouldReturnAllSavedPlayers() {
        InMemoryPlayerRepository repository = new InMemoryPlayerRepository();

        Player enzo = new Player(UUID.randomUUID(), "Enzo");
        Player carla = new Player(UUID.randomUUID(), "Carla");

        StepVerifier.create(repository.save(enzo)).verifyComplete();

        StepVerifier.create(repository.save(carla)).verifyComplete();

        StepVerifier.create(repository.findAll().collectList()).assertNext(players -> {
            assertEquals(2, players.size());
            assertTrue(players.contains(enzo));
            assertTrue(players.contains(carla));
        }).verifyComplete();
    }

    @Test
    void shouldAllowCollectingPlayersIntoList() {
        InMemoryPlayerRepository repository = new InMemoryPlayerRepository();
        Player enzo = new Player(UUID.randomUUID(), "Enzo");

        StepVerifier.create(repository.save(enzo)).verifyComplete();

        StepVerifier.create(repository.findAll().collectList()).assertNext(players -> {
            assertEquals(1, players.size());
            assertSame(enzo, players.getFirst());
        }).verifyComplete();
    }
}
