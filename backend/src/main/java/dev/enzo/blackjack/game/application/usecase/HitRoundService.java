
package dev.enzo.blackjack.game.application.usecase;

import dev.enzo.blackjack.game.application.exception.RoundNotFoundException;
import dev.enzo.blackjack.game.application.port.in.HitRoundUseCase;
import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.Round;
import dev.enzo.blackjack.game.domain.model.RoundStatus;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

public class HitRoundService implements HitRoundUseCase {

    private final RoundRepository roundRepository;
    private final RoundEventPublisher eventPublisher;

    public HitRoundService(RoundRepository roundRepository, RoundEventPublisher eventPublisher) {
        this.roundRepository = Objects.requireNonNull(roundRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public Mono<Void> hit(UUID roundId) {
        return Mono.defer(() -> {
            Objects.requireNonNull(roundId, "Round ID cannot be null");

            return roundRepository.findById(roundId)
                    .switchIfEmpty(Mono.error(new RoundNotFoundException(roundId))).flatMap(round -> {
                        round.hit();

                        return roundRepository.save(round).then(Mono.defer(() -> {
                            if (round.getStatus() == RoundStatus.FINISHED) {
                                return eventPublisher.publish(new RoundFinishedEvent(round.getRoundId(),
                                                round.getPlayerId(), round.getResult()));
                                    }

                            return Mono.empty();
                        }));
                    });
        });
    }
}


