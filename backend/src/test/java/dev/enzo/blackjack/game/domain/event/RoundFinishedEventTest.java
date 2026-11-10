
package dev.enzo.blackjack.game.domain.event;

import dev.enzo.blackjack.game.domain.model.RoundResult;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoundFinishedEventTest {

    @Test
    void shouldCreateEventWithRoundPlayerAndResult() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        RoundResult result = RoundResult.PLAYER_WIN;

        RoundFinishedEvent event = new RoundFinishedEvent(roundId, playerId, result);

        assertEquals(roundId, event.roundId());
        assertEquals(playerId, event.playerId());
        assertEquals(result, event.result());
    }

    @Test
    void shouldRejectNullRoundId() {
        UUID playerId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () ->
                        new RoundFinishedEvent(null, playerId, RoundResult.PLAYER_WIN));
    }

    @Test
    void shouldRejectNullPlayerId() {
        UUID roundId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () ->
                new RoundFinishedEvent(roundId, null, RoundResult.PLAYER_WIN));
    }

    @Test
    void shouldRejectNullResult() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () -> new RoundFinishedEvent(roundId, playerId, null));
    }

}
