package dev.enzo.blackjack.player.infrastructure.fake;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.domain.model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRoundResultRepositoryTest {
    private PlayerRepository playerRepository;
    private InMemoryRoundResultRepository resultRepository;
    private UUID playerId;
    private Player player;

    @BeforeEach
    void setUp() {
        playerRepository = new InMemoryPlayerRepository();
        resultRepository = new InMemoryRoundResultRepository(playerRepository);
        playerId = UUID.randomUUID();
        player = new Player(playerId, "Enzo");

        StepVerifier.create(playerRepository.save(player)).verifyComplete();
    }

    @Test
    void shouldRecordPlayerWin() {
        UUID roundId = UUID.randomUUID();
        StepVerifier.create(resultRepository.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN))
                .expectNext(true).verifyComplete();

        assertEquals(1, player.getWins());
        assertEquals(0, player.getLosses());
        assertEquals(0, player.getDraws());
    }

    @Test
    void shouldRecordPlayerLoss() {
        UUID roundId = UUID.randomUUID();
        StepVerifier.create(resultRepository.recordIfAbsent(roundId, playerId, RoundResult.DEALER_WIN))
                .expectNext(true).verifyComplete();

        assertEquals(0, player.getWins());
        assertEquals(1, player.getLosses());
        assertEquals(0, player.getDraws());
    }

    @Test
    void shouldRecordPlayerDraw() {
        UUID roundId = UUID.randomUUID();
        StepVerifier.create(resultRepository.recordIfAbsent(roundId, playerId, RoundResult.PUSH))
                .expectNext(true).verifyComplete();

        assertEquals(0, player.getWins());
        assertEquals(0, player.getLosses());
        assertEquals(1, player.getDraws());
    }

    @Test
    void shouldNotRecordSameRoundTwice() {
        UUID roundId = UUID.randomUUID();
        StepVerifier.create(resultRepository.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN))
                .expectNext(true).verifyComplete();

        StepVerifier.create(resultRepository.recordIfAbsent(roundId, playerId, RoundResult.PLAYER_WIN))
                .expectNext(false).verifyComplete();

        assertEquals(1, player.getWins());
        assertEquals(0, player.getLosses());
        assertEquals(0, player.getDraws());
    }

    @Test
    void shouldAccumulateResultsFromDifferentRounds() {
        StepVerifier.create(resultRepository.recordIfAbsent(UUID.randomUUID(), playerId, RoundResult.PLAYER_WIN))
                .expectNext(true).verifyComplete();

        StepVerifier.create(resultRepository.recordIfAbsent(UUID.randomUUID(), playerId, RoundResult.PLAYER_WIN))
                .expectNext(true).verifyComplete();

        StepVerifier.create(resultRepository.recordIfAbsent(UUID.randomUUID(), playerId, RoundResult.DEALER_WIN))
                .expectNext(true).verifyComplete();

        StepVerifier.create(resultRepository.recordIfAbsent(UUID.randomUUID(), playerId, RoundResult.PUSH))
                .expectNext(true).verifyComplete();

        assertEquals(2, player.getWins());
        assertEquals(1, player.getLosses());
        assertEquals(1, player.getDraws());
    }

    @Test
    void shouldRejectNonExistingPlayer() {
        UUID roundId = UUID.randomUUID();
        UUID unknownPlayerId = UUID.randomUUID();

        StepVerifier.create(resultRepository.recordIfAbsent(roundId, unknownPlayerId, RoundResult.PLAYER_WIN))
                .expectError(IllegalArgumentException.class).verify();

        assertEquals(0, player.getWins());
    }

    @Test
    void shouldRejectNullArguments() {
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(resultRepository.recordIfAbsent(null, playerId, RoundResult.PLAYER_WIN))
                .expectError(NullPointerException.class).verify();

        StepVerifier.create(resultRepository.recordIfAbsent(roundId, null, RoundResult.PLAYER_WIN))
                .expectError(NullPointerException.class).verify();

        StepVerifier.create(resultRepository.recordIfAbsent(roundId, playerId, null))
                .expectError(NullPointerException.class).verify();
    }

}
