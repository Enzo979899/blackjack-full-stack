package dev.enzo.blackjack.ranking.application.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankingEntryTest {

    @Test
    void shouldCreateValidRankingEntry() {
        UUID playerId = UUID.randomUUID();
        RankingEntry entry = new RankingEntry(playerId, "Enzo", 10, 5, 2);

        assertAll(() -> assertEquals(playerId, entry.playerId()),
                () -> assertEquals("Enzo", entry.playerName()), () -> assertEquals(10, entry.wins()),
                () -> assertEquals(5, entry.losses()), () -> assertEquals(2, entry.draws()));
    }

    @Test
    void shouldRejectNullPlayerId() {
        assertThrows(NullPointerException.class,
                () -> new RankingEntry(null, "Enzo", 10, 5, 2));

    }

    @Test
    void shouldRejectNullPlayerName() {
        assertThrows(NullPointerException.class,
                () -> new RankingEntry(UUID.randomUUID(), null, 10, 5, 2));
    }

    @Test
    void shouldRejectBlankPlayerName() {
        assertThrows(IllegalArgumentException.class,
                () -> new RankingEntry(UUID.randomUUID(), "   ", 10, 5, 2));
    }

    @Test
    void shouldRejectNegativeWins() {
        assertThrows(IllegalArgumentException.class,
                () -> new RankingEntry(UUID.randomUUID(), "Enzo", -1, 5, 2));
    }

    @Test
    void shouldRejectNegativeLosses() {
        assertThrows(IllegalArgumentException.class,
                () -> new RankingEntry(UUID.randomUUID(), "Enzo", 10, -1, 2));
    }

    @Test
    void shouldRejectNegativeDraws() {
        assertThrows(IllegalArgumentException.class,
                () -> new RankingEntry(UUID.randomUUID(), "Enzo", 10, 5, -1));
    }

    @Test
    void shouldConsiderEntriesWithSameValuesEqual() {
        UUID playerId = UUID.randomUUID();
        RankingEntry first = new RankingEntry(playerId, "Enzo", 10, 5, 2);
        RankingEntry second = new RankingEntry(playerId, "Enzo", 10, 5, 2);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
