package dev.enzo.blackjack.game.infrastructure.fake;

import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.player.application.port.in.RecordRoundResultUseCase;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryRoundEventPublisherTest {

    @Test
    void shouldForwardRoundFinishedEvent() {
        RecordRoundResultUseCase useCase = mock(RecordRoundResultUseCase.class);
        InMemoryRoundEventPublisher publisher = new InMemoryRoundEventPublisher(useCase);
        RoundFinishedEvent event = new RoundFinishedEvent(UUID.randomUUID(), UUID.randomUUID(), RoundResult.PLAYER_WIN);

        when(useCase.recordResult(event)).thenReturn(Mono.empty());
        StepVerifier.create(publisher.publish(event)).verifyComplete();

        verify(useCase, times(1)).recordResult(event);
    }

    @Test
    void shouldForwardDifferentEvents() {
        RecordRoundResultUseCase useCase = mock(RecordRoundResultUseCase.class);

        InMemoryRoundEventPublisher publisher = new InMemoryRoundEventPublisher(useCase);

        UUID playerId = UUID.randomUUID();

        RoundFinishedEvent firstEvent = new RoundFinishedEvent(UUID.randomUUID(), playerId, RoundResult.PLAYER_WIN);

        RoundFinishedEvent secondEvent = new RoundFinishedEvent(UUID.randomUUID(), playerId, RoundResult.DEALER_WIN);
        when(useCase.recordResult(firstEvent)).thenReturn(Mono.empty());
        when(useCase.recordResult(secondEvent)).thenReturn(Mono.empty());
        StepVerifier.create(publisher.publish(firstEvent)).verifyComplete();
        StepVerifier.create(publisher.publish(secondEvent)).verifyComplete();

        var inOrder = inOrder(useCase);

        inOrder.verify(useCase).recordResult(firstEvent);
        inOrder.verify(useCase).recordResult(secondEvent);
    }

    @Test
    void shouldRejectNullUseCase() {
        assertThrows(NullPointerException.class, () -> new InMemoryRoundEventPublisher(null));

    }

    @Test
    void shouldPropagateUseCaseFailure() {
        RecordRoundResultUseCase useCase = mock(RecordRoundResultUseCase.class);
        InMemoryRoundEventPublisher publisher = new InMemoryRoundEventPublisher(useCase);
        RoundFinishedEvent event = new RoundFinishedEvent(UUID.randomUUID(), UUID.randomUUID(), RoundResult.PUSH);

        when(useCase.recordResult(event)).thenReturn(Mono.error(new IllegalStateException("Result processing failed")));

        StepVerifier.create(publisher.publish(event)).expectErrorMatches(error ->
                error instanceof IllegalStateException && "Result processing failed".equals(error.getMessage()))
                .verify();

        verify(useCase).recordResult(event);
    }

}
