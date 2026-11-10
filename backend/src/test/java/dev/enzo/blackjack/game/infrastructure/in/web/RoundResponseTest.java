package dev.enzo.blackjack.game.infrastructure.in.web;

import dev.enzo.blackjack.game.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoundResponseTest {

    @Test
    void shouldHideDealerSecondCardDuringPlayerTurn() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Round round = mock(Round.class);

        Card playerFirstCard = new Card(Suit.HEARTS, Rank.TEN);
        Card playerSecondCard = new Card(Suit.SPADES, Rank.EIGHT);

        Card dealerFirstCard = new Card(Suit.DIAMONDS, Rank.KING);
        Card dealerSecondCard = new Card(Suit.CLUBS, Rank.SEVEN);

        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getStatus()).thenReturn(RoundStatus.PLAYER_TURN);

        when(round.getPlayerHand()).thenReturn(new HandSnapshot(List.of(playerFirstCard, playerSecondCard)));

        when(round.getDealerHand()).thenReturn(new HandSnapshot(List.of(dealerFirstCard, dealerSecondCard)));

        RoundResponse response = RoundResponse.from(round);

        assertEquals(roundId, response.roundId());
        assertEquals(playerId, response.playerId());
        assertEquals(RoundStatus.PLAYER_TURN, response.status());

        assertEquals(2, response.playerHand().cards().size());
        assertEquals(18, response.playerHand().score());

        assertEquals(1, response.dealerHand().cards().size());
        assertEquals(10, response.dealerHand().score());

        assertEquals(Rank.KING, response.dealerHand().cards().getFirst().rank());

        assertNull(response.result());
    }

    @Test
    void shouldRevealDealerCardsWhenRoundIsFinished() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Round round = mock(Round.class);

        Card playerFirstCard = new Card(Suit.HEARTS, Rank.TEN);
        Card playerSecondCard = new Card(Suit.SPADES, Rank.EIGHT);

        Card dealerFirstCard = new Card(Suit.DIAMONDS, Rank.KING);
        Card dealerSecondCard = new Card(Suit.CLUBS, Rank.SEVEN);

        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getStatus()).thenReturn(RoundStatus.FINISHED);
        when(round.getResult()).thenReturn(RoundResult.PLAYER_WIN);

        when(round.getPlayerHand()).thenReturn(new HandSnapshot(List.of(playerFirstCard, playerSecondCard)));

        when(round.getDealerHand()).thenReturn(new HandSnapshot(List.of(dealerFirstCard, dealerSecondCard)));

        RoundResponse response = RoundResponse.from(round);

        assertEquals(RoundStatus.FINISHED, response.status());

        assertEquals(2, response.playerHand().cards().size());
        assertEquals(18, response.playerHand().score());

        assertEquals(2, response.dealerHand().cards().size());
        assertEquals(17, response.dealerHand().score());

        assertEquals(Rank.SEVEN, response.dealerHand().cards().get(1).rank());

        assertEquals(RoundResult.PLAYER_WIN, response.result());
    }

}
