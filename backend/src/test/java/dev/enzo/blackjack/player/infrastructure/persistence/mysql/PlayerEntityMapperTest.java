package dev.enzo.blackjack.player.infrastructure.persistence.mysql;

import dev.enzo.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerEntityMapperTest {

    @Test
    void shouldMapPlayerToEntity() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Enzo", 5, 2, 1);

        PlayerEntity entity = PlayerEntityMapper.toEntity(player);

        assertEquals(id.toString(), entity.getId());
        assertEquals("Enzo", entity.getName());
        assertEquals(5, entity.getWins());
        assertEquals(2, entity.getLosses());
        assertEquals(1, entity.getDraws());
    }

    @Test
    void shouldMapEntityToPlayer() {
        UUID id = UUID.randomUUID();
        PlayerEntity entity = new PlayerEntity(id.toString(), "Enzo", 5, 2, 1);

        Player player = PlayerEntityMapper.toDomain(entity);

        assertEquals(id, player.getId());
        assertEquals("Enzo", player.getName());
        assertEquals(5, player.getWins());
        assertEquals(2, player.getLosses());
        assertEquals(1, player.getDraws());
    }
}
