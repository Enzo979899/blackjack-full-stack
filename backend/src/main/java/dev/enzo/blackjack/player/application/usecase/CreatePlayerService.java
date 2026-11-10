package dev.enzo.blackjack.player.application.usecase;

import dev.enzo.blackjack.player.application.dto.CreatePlayerResult;
import dev.enzo.blackjack.player.application.port.in.CreatePlayerUseCase;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.domain.model.Player;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

public class CreatePlayerService implements CreatePlayerUseCase {
    private final PlayerRepository playerRepository;

    public CreatePlayerService(PlayerRepository playerRepository) {
        this.playerRepository = Objects.requireNonNull(playerRepository);
    }

    @Override
    public Mono<CreatePlayerResult> createPlayer(String name) {
        return Mono.defer(() -> {
            Player player = new Player(UUID.randomUUID(), name);

            return playerRepository.save(player).thenReturn(new CreatePlayerResult(player.getId(), player.getName()));
        });
    }

}
