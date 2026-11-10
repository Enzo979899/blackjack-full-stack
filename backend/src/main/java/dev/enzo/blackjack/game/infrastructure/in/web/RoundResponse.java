package dev.enzo.blackjack.game.infrastructure.in.web;

import dev.enzo.blackjack.game.domain.model.Round;
import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.game.domain.model.RoundStatus;

import java.util.UUID;

public record RoundResponse(UUID roundId, UUID playerId, RoundStatus status, HandResponse playerHand,
                            HandResponse dealerHand, RoundResult result) {

    public static RoundResponse from(Round round) {
        boolean finished = round.getStatus() == RoundStatus.FINISHED;

        HandResponse playerHand = HandResponse.from(round.getPlayerHand());

        HandResponse dealerHand;

        if (finished) {
            dealerHand = HandResponse.from(round.getDealerHand());
        } else {
            var dealerCards = round.getDealerHand().getCards();

            dealerHand = HandResponse.fromVisibleCards(dealerCards.isEmpty()
                    ? java.util.List.of() : java.util.List.of(dealerCards.getFirst()));
        }

        return new RoundResponse(round.getRoundId(), round.getPlayerId(), round.getStatus(), playerHand, dealerHand,
                finished ? round.getResult() : null);
    }
}