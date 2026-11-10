package dev.enzo.blackjack.ranking.infrastructure.fake;

import dev.enzo.blackjack.player.domain.model.Player;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryPlayerRepository;
import dev.enzo.blackjack.ranking.application.dto.RankingEntry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRankingRepositoryTest {
    private InMemoryPlayerRepository playerRepository;
    private InMemoryRankingRepository rankingRepository;

    @BeforeEach
    void setUp() {
        playerRepository = new InMemoryPlayerRepository();
        rankingRepository = new InMemoryRankingRepository(playerRepository);
    }

    @Test
    void shouldReturnEmptyListWhenNoPlayersExist() {
        StepVerifier.create(rankingRepository.findAll()).verifyComplete();
    }

    @Test
    void shouldConvertPlayerToRankingEntry() {
        UUID playerId = UUID.randomUUID();
        Player player = new Player(playerId, "Enzo");

        StepVerifier.create(playerRepository.save(player)).verifyComplete();

        StepVerifier.create(rankingRepository.findAll()).assertNext(entry -> assertAll(() ->
                assertEquals(playerId, entry.playerId()), () ->
                assertEquals("Enzo", entry.playerName()), () ->
                assertEquals(0, entry.wins()), () ->
                assertEquals(0, entry.losses()), () ->
                assertEquals(0, entry.draws()))).verifyComplete();
    }

    @Test
    void shouldReturnAllPlayersAsRankingEntries() {
        Player enzo = new Player(UUID.randomUUID(), "Enzo");
        Player carla = new Player(UUID.randomUUID(), "Carla");

        StepVerifier.create(playerRepository.save(enzo)).verifyComplete();

        StepVerifier.create(playerRepository.save(carla)).verifyComplete();

        StepVerifier.create(rankingRepository.findAll().collectList()).assertNext(entries -> {
            assertEquals(2, entries.size());
            assertTrue(entries.stream().anyMatch(entry -> entry.playerId().equals(enzo.getId())));
            assertTrue(entries.stream().anyMatch(entry -> entry.playerId().equals(carla.getId())));
        }).verifyComplete();
    }

    @Test
    void shouldReflectUpdatedPlayerStatistics() {
        Player player = new Player(UUID.randomUUID(), "Enzo");

        StepVerifier.create(playerRepository.save(player)).verifyComplete();

        player.recordWin();
        player.recordWin();
        player.recordLoss();
        player.recordDraw();

        StepVerifier.create(playerRepository.save(player)).verifyComplete();

        StepVerifier.create(rankingRepository.findAll()).assertNext(entry -> assertAll(() ->
                assertEquals(2, entry.wins()), () ->
                assertEquals(1, entry.losses()), () ->
                assertEquals(1, entry.draws()))).verifyComplete();
    }

    @Test
    void shouldReturnSnapshotOfPlayerStatistics() {
        Player player = new Player(UUID.randomUUID(), "Enzo");

        StepVerifier.create(playerRepository.save(player)).verifyComplete();

        RankingEntry[] originalEntry = new RankingEntry[1];

        StepVerifier.create(rankingRepository.findAll()).assertNext(entry ->
                originalEntry[0] = entry).verifyComplete();

        player.recordWin();

        StepVerifier.create(playerRepository.save(player)).verifyComplete();

        StepVerifier.create(rankingRepository.findAll()).assertNext(updatedEntry -> {
            assertEquals(0, originalEntry[0].wins());
            assertEquals(1, updatedEntry.wins());
        }).verifyComplete();
    }

    @Test
    void shouldRejectNullPlayerRepository() {
        assertThrows(NullPointerException.class, () -> new InMemoryRankingRepository(null));
    }

}
