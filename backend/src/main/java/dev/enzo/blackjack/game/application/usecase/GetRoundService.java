package dev.enzo.blackjack.game.application.usecase;

import dev.enzo.blackjack.game.application.exception.RoundNotFoundException;
import dev.enzo.blackjack.game.application.port.in.GetRoundUseCase;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.model.Round;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

public class GetRoundService implements GetRoundUseCase {

    private final RoundRepository roundRepository;

    public GetRoundService(RoundRepository roundRepository) {
        this.roundRepository = Objects.requireNonNull(roundRepository);
    }

    @Override
    public Mono<Round> getRound(UUID roundId) {
        return Mono.defer(() -> {
            Objects.requireNonNull(roundId, "Round ID cannot be null");

            return roundRepository.findById(roundId).switchIfEmpty(Mono.error(new RoundNotFoundException(roundId)));
        });
    }

}
