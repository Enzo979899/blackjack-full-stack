package dev.enzo.blackjack.game.infrastructure.persistence.mongo;

import dev.enzo.blackjack.game.domain.model.Card;
import dev.enzo.blackjack.game.domain.model.Deck;
import dev.enzo.blackjack.game.domain.model.Round;

import java.util.List;
import java.util.UUID;

final class RoundDocumentMapper {

    private RoundDocumentMapper() {
    }

    static CardDocument toCardDocument(Card card) {
        return new CardDocument(card.getSuit(), card.getRank());
    }

    static Card toCard(CardDocument document) {
        return new Card(document.suit(), document.rank());
    }
    static List<CardDocument> toCardDocuments(List<Card> cards) {
        return cards.stream().map(RoundDocumentMapper::toCardDocument).toList();
    }

    public static List<Card> toCards(List<CardDocument> documents) {
        return documents.stream().map(RoundDocumentMapper::toCard).toList();
    }

    static RoundDocument toDocument(Round round) {
        return new RoundDocument(round.getRoundId().toString(), round.getPlayerId().toString(),
                toCardDocuments(round.getPlayerHand().getCards()), toCardDocuments(round.getDealerHand().getCards()),
                toCardDocuments(round.getRemainingCards()), round.getStatus(), round.getResult());
    }

    static Round toDomain(RoundDocument document) {
        return Round.restore(UUID.fromString(document.getId()), UUID.fromString(document.getPlayerId()),
                toCards(document.getPlayerCards()), toCards(document.getDealerCards()),
                new Deck(toCards(document.getRemainingCards())), document.getStatus(), document.getResult());
    }
}
