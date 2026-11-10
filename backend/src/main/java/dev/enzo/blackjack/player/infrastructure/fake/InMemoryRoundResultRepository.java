package dev.enzo.blackjack.player.infrastructure.fake;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.application.port.out.RoundResultRepository;
import dev.enzo.blackjack.player.domain.model.Player;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class InMemoryRoundResultRepository implements RoundResultRepository {
    private final PlayerRepository playerRepository;
    private final Set<UUID> processedRoundIds = new HashSet<>();

    public InMemoryRoundResultRepository(PlayerRepository playerRepository) {
        this.playerRepository = Objects.requireNonNull(playerRepository);
    }

    @Override
    public Mono<Boolean> recordIfAbsent(UUID roundId, UUID playerId, RoundResult result) {

        return Mono.defer(() -> {
            Objects.requireNonNull(roundId, "Round ID cannot be null");
            Objects.requireNonNull(playerId, "Player ID cannot be null");
            Objects.requireNonNull(result, "Round result cannot be null");

            synchronized (processedRoundIds) {
                if (!processedRoundIds.add(roundId)) {
                    return Mono.just(false);
                }
            }

            return playerRepository.findById(playerId)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Player not found: " + playerId)))
                    .flatMap(player -> {
                        switch (result) {
                            case PLAYER_WIN -> player.recordWin();
                            case DEALER_WIN -> player.recordLoss();
                            case PUSH -> player.recordDraw();
                        }

                        return playerRepository.save(player)
                                .thenReturn(true);
                    }).doOnError(error -> {
                        synchronized (processedRoundIds) {
                            processedRoundIds.remove(roundId);
                        }
                    });
        });
    }

}


