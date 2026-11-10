package dev.enzo.blackjack.game.infrastructure.fake;

import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.player.application.port.in.RecordRoundResultUseCase;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class InMemoryRoundEventPublisher implements RoundEventPublisher {

    private final RecordRoundResultUseCase recordRoundResultUseCase;

    public InMemoryRoundEventPublisher(RecordRoundResultUseCase recordRoundResultUseCase) {
        this.recordRoundResultUseCase = Objects.requireNonNull(recordRoundResultUseCase,
                "RecordRoundResultUseCase cannot be null");
    }

    @Override
    public Mono<Void> publish(RoundFinishedEvent event) {
        return Mono.defer(() -> recordRoundResultUseCase.recordResult(event));
    }
}
