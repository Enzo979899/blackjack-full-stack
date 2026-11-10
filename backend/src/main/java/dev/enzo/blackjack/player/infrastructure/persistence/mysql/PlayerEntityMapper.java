package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import dev.enzo.blackjack.player.domain.model.Player;

import java.util.UUID;

public final class PlayerEntityMapper {

    private PlayerEntityMapper() {
    }

    public static PlayerEntity toEntity(Player player) {
        return new PlayerEntity(player.getId().toString(), player.getName(), player.getWins(), player.getLosses(),
                player.getDraws());
    }

    public static Player toDomain(PlayerEntity entity) {
        return new Player(UUID.fromString(entity.getId()), entity.getName(), entity.getWins(), entity.getLosses(),
                entity.getDraws());
    }
}
