package dev.enzo.blackjack.game.application.usecase;

import dev.enzo.blackjack.game.application.exception.RoundNotFoundException;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.model.Round;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

class GetRoundServiceTest {

    @Test
    void shouldGetExistingRound() {
        UUID roundId = UUID.randomUUID();

        RoundRepository roundRepository = mock(RoundRepository.class);
        Round round = mock(Round.class);

        when(roundRepository.findById(roundId)).thenReturn(Mono.just(round));

        GetRoundService service = new GetRoundService(roundRepository);

        StepVerifier.create(service.getRound(roundId)).expectNext(round).verifyComplete();

        verify(roundRepository).findById(roundId);
        verifyNoMoreInteractions(roundRepository);
    }

    @Test
    void shouldRejectNonExistingRound() {
        UUID roundId = UUID.randomUUID();

        RoundRepository roundRepository = mock(RoundRepository.class);

        when(roundRepository.findById(roundId)).thenReturn(Mono.empty());

        GetRoundService service = new GetRoundService(roundRepository);

        StepVerifier.create(service.getRound(roundId)).expectError(RoundNotFoundException.class).verify();

        verify(roundRepository).findById(roundId);
        verifyNoMoreInteractions(roundRepository);
    }

}

