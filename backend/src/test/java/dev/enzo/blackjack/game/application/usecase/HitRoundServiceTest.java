package dev.enzo.blackjack.game.application.usecase;

import dev.enzo.blackjack.game.application.exception.RoundNotFoundException;
import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.Round;
import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.game.domain.model.RoundStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HitRoundServiceTest {

    private RoundRepository repository;
    private RoundEventPublisher eventPublisher;
    private HitRoundService service;

    @BeforeEach
    void setUp() {
        repository = mock(RoundRepository.class);
        eventPublisher = mock(RoundEventPublisher.class);
        service = new HitRoundService(repository, eventPublisher);
    }

    @Test
    void shouldHitAndSaveExistingRound() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        when(repository.save(round)).thenReturn(Mono.empty());
        when(round.getStatus()).thenReturn(RoundStatus.PLAYER_TURN);

        StepVerifier.create(service.hit(roundId)).verifyComplete();

        var inOrder = inOrder(repository, round);

        inOrder.verify(repository).findById(roundId);
        inOrder.verify(round).hit();
        inOrder.verify(repository).save(round);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectNonExistingRound() {
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.empty());

        StepVerifier.create(service.hit(roundId)).expectError(RoundNotFoundException.class).verify();
        verify(repository).findById(roundId);
        verify(repository, never()).save(any(Round.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectNullRoundId() {
        StepVerifier.create(service.hit(null)).expectError(NullPointerException.class).verify();

        verifyNoInteractions(repository, eventPublisher);
    }

    @Test
    void shouldNotSaveWhenDomainRejectsHit() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        doThrow(new IllegalStateException("Round already finished")).when(round).hit();
        StepVerifier.create(service.hit(roundId)).expectError(IllegalStateException.class).verify();
        verify(round).hit();
        verify(repository, never()).save(any(Round.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectNullDependencies() {
        assertThrows(NullPointerException.class, () -> new HitRoundService(null, eventPublisher));

        assertThrows(NullPointerException.class, () -> new HitRoundService(repository, null));
    }

    @Test
    void shouldPublishEventWhenPlayerBusts() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        when(repository.save(round)).thenReturn(Mono.empty());
        when(eventPublisher.publish(any(RoundFinishedEvent.class))).thenReturn(Mono.empty());
        when(round.getStatus()).thenReturn(RoundStatus.FINISHED);
        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getResult()).thenReturn(RoundResult.DEALER_WIN);

        StepVerifier.create(service.hit(roundId)).verifyComplete();

        ArgumentCaptor<RoundFinishedEvent> captor = ArgumentCaptor.forClass(RoundFinishedEvent.class);

        var inOrder = inOrder(repository, eventPublisher);

        inOrder.verify(repository).save(round);
        inOrder.verify(eventPublisher).publish(captor.capture());

        RoundFinishedEvent event = captor.getValue();

        assertEquals(roundId, event.roundId());
        assertEquals(playerId, event.playerId());
        assertEquals(RoundResult.DEALER_WIN, event.result());
    }

    @Test
    void shouldNotPublishEventWhenRoundIsStillActive() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        when(repository.save(round)).thenReturn(Mono.empty());
        when(round.getStatus()).thenReturn(RoundStatus.PLAYER_TURN);

        StepVerifier.create(service.hit(roundId)).verifyComplete();

        verify(round).hit();
        verify(repository).save(round);
        verifyNoInteractions(eventPublisher);
    }

}

