package dev.enzo.blackjack.game.application.usecase;

import dev.enzo.blackjack.game.application.port.in.StartRoundUseCase;
import dev.enzo.blackjack.game.application.port.out.DeckFactory;
import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.Round;
import dev.enzo.blackjack.game.domain.model.RoundStatus;
import dev.enzo.blackjack.player.application.exception.PlayerNotFoundException;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

public class StartRoundService implements StartRoundUseCase {
    private final PlayerRepository playerRepository;
    private final RoundRepository roundRepository;
    private final RoundEventPublisher eventPublisher;
    private final DeckFactory deckFactory;

    public StartRoundService(PlayerRepository playerRepository, RoundRepository roundRepository,
                             RoundEventPublisher eventPublisher, DeckFactory deckFactory) {
        this.playerRepository = Objects.requireNonNull(playerRepository);
        this.roundRepository = Objects.requireNonNull(roundRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.deckFactory = Objects.requireNonNull(deckFactory);
    }

    @Override
    public Mono<UUID> startRound(UUID playerId) {
        return Mono.defer(() -> {
            Objects.requireNonNull(playerId, "Player ID cannot be null");

            return playerRepository.findById(playerId)
                    .switchIfEmpty(Mono.error(new PlayerNotFoundException(playerId)))
                    .flatMap(player -> {
                        UUID roundId = UUID.randomUUID();
                        Round round = new Round(roundId, player.getId(), deckFactory.createDeck());

                        round.start();

                        return roundRepository.save(round).then(Mono.defer(() -> {
                            if (round.getStatus() == RoundStatus.FINISHED) {
                                RoundFinishedEvent event = new RoundFinishedEvent(round.getRoundId(),
                                        round.getPlayerId(), round.getResult());

                                        return eventPublisher.publish(event);
                                    }
                                    return Mono.empty();
                                })).thenReturn(roundId);
                    });
        });
    }

}
