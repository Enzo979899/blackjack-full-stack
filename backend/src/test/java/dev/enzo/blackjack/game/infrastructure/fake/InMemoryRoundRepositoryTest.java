package dev.enzo.blackjack.game.infrastructure.fake;

import dev.enzo.blackjack.game.domain.model.Deck;
import dev.enzo.blackjack.game.domain.model.Round;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRoundRepositoryTest {

    @Test
    void shouldSaveAndFindRoundById() {
        InMemoryRoundRepository repository = new InMemoryRoundRepository();
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Round round = new Round(roundId, playerId, new Deck());

        StepVerifier.create(repository.save(round)).verifyComplete();

        StepVerifier.create(repository.findById(roundId)).assertNext(savedRound -> assertSame(round, savedRound))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenRoundDoesNotExist() {
        InMemoryRoundRepository repository = new InMemoryRoundRepository();
        UUID roundId = UUID.randomUUID();

        StepVerifier.create(repository.findById(roundId)).verifyComplete();
    }

    @Test
    void shouldStoreMultipleRoundsIndependently() {
        InMemoryRoundRepository repository = new InMemoryRoundRepository();
        UUID playerId = UUID.randomUUID();
        UUID firstRoundId = UUID.randomUUID();
        UUID secondRoundId = UUID.randomUUID();
        Round firstRound = new Round(firstRoundId, playerId, new Deck());
        Round secondRound = new Round(secondRoundId, playerId, new Deck());

        StepVerifier.create(repository.save(firstRound)).verifyComplete();

        StepVerifier.create(repository.save(secondRound)).verifyComplete();

        StepVerifier.create(repository.findById(firstRoundId))
                .assertNext(savedRound -> assertSame(firstRound, savedRound)).verifyComplete();

        StepVerifier.create(repository.findById(secondRoundId))
                .assertNext(savedRound -> assertSame(secondRound, savedRound)).verifyComplete();
    }

    @Test
    void shouldReplaceRoundWhenSavingSameId() {
        InMemoryRoundRepository repository = new InMemoryRoundRepository();
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Round originalRound = new Round(roundId, playerId, new Deck());
        Round updatedRound = new Round(roundId, playerId, new Deck());

        StepVerifier.create(repository.save(originalRound)).verifyComplete();

        StepVerifier.create(repository.save(updatedRound)).verifyComplete();

        StepVerifier.create(repository.findById(roundId))
                .assertNext(savedRound -> assertSame(updatedRound, savedRound)).verifyComplete();
    }

}
