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
import static org.mockito.Mockito.*;

class StandRoundServiceTest {

    private RoundRepository repository;
    private RoundEventPublisher eventPublisher;
    private StandRoundService service;

    @BeforeEach
    void setUp() {
        repository = mock(RoundRepository.class);
        eventPublisher = mock(RoundEventPublisher.class);
        service = new StandRoundService(repository, eventPublisher);
    }

    @Test
    void shouldStandAndSaveExistingRound() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        when(repository.save(round)).thenReturn(Mono.empty());
        when(eventPublisher.publish(any(RoundFinishedEvent.class))).thenReturn(Mono.empty());
        when(round.getStatus()).thenReturn(RoundStatus.FINISHED);
        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getResult()).thenReturn(RoundResult.PLAYER_WIN);

        StepVerifier.create(service.stand(roundId)).verifyComplete();

        var inOrder = inOrder(repository, round, eventPublisher);

        inOrder.verify(repository).findById(roundId);
        inOrder.verify(round).stand();
        inOrder.verify(round).playDealerTurn();
        inOrder.verify(repository).save(round);
        inOrder.verify(eventPublisher).publish(any(RoundFinishedEvent.class));
    }

    @Test
    void shouldRejectNonExistingRound() {
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.empty());

        StepVerifier.create(service.stand(roundId)).expectError(RoundNotFoundException.class).verify();

        verify(repository).findById(roundId);
        verify(repository, never()).save(any(Round.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectNullRoundId() {
        StepVerifier.create(service.stand(null)).expectError(NullPointerException.class).verify();

        verifyNoInteractions(repository, eventPublisher);
    }

    @Test
    void shouldNotSaveWhenDomainRejectsStand() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));

        doThrow(new IllegalStateException("Round already finished")).when(round).stand();

        StepVerifier.create(service.stand(roundId)).expectError(IllegalStateException.class).verify();
        verify(round).stand();
        verify(round, never()).playDealerTurn();
        verify(repository, never()).save(any(Round.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldNotSaveWhenDealerTurnFails() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));

        doThrow(new IllegalStateException("Dealer turn failed")).when(round).playDealerTurn();

        StepVerifier.create(service.stand(roundId)).expectError(IllegalStateException.class).verify();
        verify(round).stand();
        verify(round).playDealerTurn();
        verify(repository, never()).save(any(Round.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectNullDependencies() {
        assertThrows(NullPointerException.class, () -> new StandRoundService(null, eventPublisher));

        assertThrows(NullPointerException.class, () -> new StandRoundService(repository, null));
    }

    @Test
    void shouldPublishEventWhenRoundFinishes() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        when(repository.save(round)).thenReturn(Mono.empty());
        when(eventPublisher.publish(any(RoundFinishedEvent.class))).thenReturn(Mono.empty());
        when(round.getStatus()).thenReturn(RoundStatus.FINISHED);
        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getResult()).thenReturn(RoundResult.PLAYER_WIN);

        StepVerifier.create(service.stand(roundId)).verifyComplete();

        ArgumentCaptor<RoundFinishedEvent> captor = ArgumentCaptor.forClass(RoundFinishedEvent.class);

        var inOrder = inOrder(repository, eventPublisher);

        inOrder.verify(repository).save(round);
        inOrder.verify(eventPublisher).publish(captor.capture());

        RoundFinishedEvent event = captor.getValue();

        assertEquals(roundId, event.roundId());
        assertEquals(playerId, event.playerId());
        assertEquals(RoundResult.PLAYER_WIN, event.result());
        verify(eventPublisher, times(1)).publish(any(RoundFinishedEvent.class));
    }

    @Test
    void shouldNotPublishEventWhenRoundIsNotFinished() {
        Round round = mock(Round.class);
        UUID roundId = UUID.randomUUID();

        when(repository.findById(roundId)).thenReturn(Mono.just(round));
        when(repository.save(round)).thenReturn(Mono.empty());
        when(round.getStatus()).thenReturn(RoundStatus.DEALER_TURN);

        StepVerifier.create(service.stand(roundId)).verifyComplete();

        verify(round).stand();
        verify(round).playDealerTurn();
        verify(repository).save(round);
        verifyNoInteractions(eventPublisher);
    }

}

