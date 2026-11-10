package dev.enzo.blackjack.player.application.usecase;

import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.player.application.port.in.RecordRoundResultUseCase;
import dev.enzo.blackjack.player.application.port.out.RoundResultRepository;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class RecordRoundResultService implements RecordRoundResultUseCase {
    private final RoundResultRepository roundResultRepository;

    public RecordRoundResultService(RoundResultRepository roundResultRepository) {
        this.roundResultRepository =
                Objects.requireNonNull(roundResultRepository, "RoundResultRepository cannot be null");
    }

    @Override
    public Mono<Void> recordResult(RoundFinishedEvent event) {
        return Mono.defer(() -> {
            Objects.requireNonNull(event, "RoundFinishedEvent cannot be null");

            return roundResultRepository.recordIfAbsent(event.roundId(), event.playerId(), event.result()).then();
        });
    }

}
