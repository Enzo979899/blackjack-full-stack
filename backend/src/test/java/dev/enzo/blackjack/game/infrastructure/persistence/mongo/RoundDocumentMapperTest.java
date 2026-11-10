package dev.enzo.blackjack.game.infrastructure.persistence.mongo;

import dev.enzo.blackjack.game.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testng.AssertJUnit.assertNull;

class RoundDocumentMapperTest {

    @Test
    void shouldMapCardToCardDocument() {
        Card card = new Card(Suit.HEARTS, Rank.AS);

        CardDocument document = RoundDocumentMapper.toCardDocument(card);

        assertEquals(Suit.HEARTS, document.suit());
        assertEquals(Rank.AS, document.rank());
    }

    @Test
    void shouldMapCardDocumentToCard() {
        CardDocument document = new CardDocument(Suit.SPADES, Rank.KING);

        Card card = RoundDocumentMapper.toCard(document);

        assertEquals(Suit.SPADES, card.getSuit());
        assertEquals(Rank.KING, card.getRank());
    }

    @Test
    void shouldMapCardListToCardDocumentList() {
        List<Card> cards = List.of(new Card(Suit.HEARTS, Rank.AS), new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.CLUBS, Rank.FIVE));

        List<CardDocument> documents = RoundDocumentMapper.toCardDocuments(cards);

        assertEquals(3, documents.size());

        assertEquals(Suit.HEARTS, documents.get(0).suit());
        assertEquals(Rank.AS, documents.get(0).rank());

        assertEquals(Suit.SPADES, documents.get(1).suit());
        assertEquals(Rank.KING, documents.get(1).rank());

        assertEquals(Suit.CLUBS, documents.get(2).suit());
        assertEquals(Rank.FIVE, documents.get(2).rank());
    }

    @Test
    void shouldMapCardDocumentListToCardList() {
        List<CardDocument> documents = List.of(new CardDocument(Suit.HEARTS, Rank.AS),
                new CardDocument(Suit.SPADES, Rank.KING), new CardDocument(Suit.CLUBS, Rank.FIVE));

        List<Card> cards = RoundDocumentMapper.toCards(documents);

        assertEquals(3, cards.size());

        assertEquals(Suit.HEARTS, cards.get(0).getSuit());
        assertEquals(Rank.AS, cards.get(0).getRank());

        assertEquals(Suit.SPADES, cards.get(1).getSuit());
        assertEquals(Rank.KING, cards.get(1).getRank());

        assertEquals(Suit.CLUBS, cards.get(2).getSuit());
        assertEquals(Rank.FIVE, cards.get(2).getRank());
    }

    @Test
    void shouldMapRoundToDocument() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Round round = new Round(roundId, playerId, new Deck());

        RoundDocument document = RoundDocumentMapper.toDocument(round);

        assertEquals(roundId.toString(), document.getId());
        assertEquals(playerId.toString(), document.getPlayerId());
        assertEquals(RoundStatus.WAITING_TO_START, document.getStatus());
        assertNull(document.getResult());
    }

    @Test
    void shouldPreserveAllCardsWhenMappingRoundToDocument() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        List<Card> playerCards = List.of(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.SPADES, Rank.SIX));

        List<Card> dealerCards = List.of(new Card(Suit.CLUBS, Rank.NINE), new Card(Suit.DIAMONDS, Rank.SEVEN));

        List<Card> remainingCards = List.of(new Card(Suit.HEARTS, Rank.THREE), new Card(Suit.CLUBS, Rank.FIVE));

        Round round = Round.restore(roundId, playerId, playerCards, dealerCards, new Deck(remainingCards),
                RoundStatus.PLAYER_TURN, null);

        RoundDocument document = RoundDocumentMapper.toDocument(round);

        assertEquals(2, document.getPlayerCards().size());
        assertEquals(Suit.HEARTS, document.getPlayerCards().get(0).suit());
        assertEquals(Rank.TEN, document.getPlayerCards().get(0).rank());
        assertEquals(Suit.SPADES, document.getPlayerCards().get(1).suit());
        assertEquals(Rank.SIX, document.getPlayerCards().get(1).rank());

        assertEquals(2, document.getDealerCards().size());
        assertEquals(Suit.CLUBS, document.getDealerCards().get(0).suit());
        assertEquals(Rank.NINE, document.getDealerCards().get(0).rank());
        assertEquals(Suit.DIAMONDS, document.getDealerCards().get(1).suit());
        assertEquals(Rank.SEVEN, document.getDealerCards().get(1).rank());

        assertEquals(2, document.getRemainingCards().size());
        assertEquals(Suit.HEARTS, document.getRemainingCards().get(0).suit());
        assertEquals(Rank.THREE, document.getRemainingCards().get(0).rank());
        assertEquals(Suit.CLUBS, document.getRemainingCards().get(1).suit());
        assertEquals(Rank.FIVE, document.getRemainingCards().get(1).rank());
    }

    @Test
    void shouldMapRoundDocumentToDomain() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        RoundDocument document = new RoundDocument(roundId.toString(), playerId.toString(), List.of(), List.of(),
                List.of(new CardDocument(Suit.HEARTS, Rank.AS), new CardDocument(Suit.SPADES, Rank.KING)),
                RoundStatus.WAITING_TO_START, null);

        Round round = RoundDocumentMapper.toDomain(document);

        assertEquals(roundId, round.getRoundId());
        assertEquals(playerId, round.getPlayerId());
        assertEquals(RoundStatus.WAITING_TO_START, round.getStatus());
        assertNull(round.getResult());

        assertEquals(0, round.getPlayerHand().getCards().size());
        assertEquals(0, round.getDealerHand().getCards().size());

        assertEquals(2, round.getRemainingCards().size());
        assertEquals(Suit.HEARTS, round.getRemainingCards().get(0).getSuit());
        assertEquals(Rank.AS, round.getRemainingCards().get(0).getRank());
        assertEquals(Suit.SPADES, round.getRemainingCards().get(1).getSuit());
        assertEquals(Rank.KING, round.getRemainingCards().get(1).getRank());
    }

    @Test
    void shouldContinuePlayingAfterRoundIsMappedAndRestored() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Round originalRound = Round.restore(roundId, playerId, List.of(new Card(Suit.HEARTS, Rank.TEN),
                        new Card(Suit.SPADES, Rank.SIX)), List.of(new Card(Suit.CLUBS, Rank.NINE),
                        new Card(Suit.DIAMONDS, Rank.SEVEN)), new Deck(List.of(new Card(Suit.HEARTS, Rank.THREE),
                        new Card(Suit.CLUBS, Rank.FIVE))), RoundStatus.PLAYER_TURN, null);

        RoundDocument document = RoundDocumentMapper.toDocument(originalRound);

        Round restoredRound = RoundDocumentMapper.toDomain(document);

        restoredRound.hit();

        assertEquals(roundId, restoredRound.getRoundId());
        assertEquals(playerId, restoredRound.getPlayerId());

        assertEquals(3, restoredRound.getPlayerHand().getCards().size());
        assertEquals(Rank.THREE, restoredRound.getPlayerHand().getCards().get(2).getRank());
        assertEquals(Suit.HEARTS, restoredRound.getPlayerHand().getCards().get(2).getSuit());

        assertEquals(1, restoredRound.getRemainingCards().size());
        assertEquals(Suit.CLUBS, restoredRound.getRemainingCards().getFirst().getSuit());
        assertEquals(Rank.FIVE, restoredRound.getRemainingCards().getFirst().getRank());

        assertEquals(RoundStatus.PLAYER_TURN, restoredRound.getStatus());
        assertNull(restoredRound.getResult());
    }
}
