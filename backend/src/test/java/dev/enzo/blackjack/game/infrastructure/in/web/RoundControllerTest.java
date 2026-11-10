package dev.enzo.blackjack.game.infrastructure.in.web;

import dev.enzo.blackjack.game.application.exception.RoundNotFoundException;
import dev.enzo.blackjack.game.application.port.in.GetRoundUseCase;
import dev.enzo.blackjack.game.application.port.in.HitRoundUseCase;
import dev.enzo.blackjack.game.application.port.in.StandRoundUseCase;
import dev.enzo.blackjack.game.application.port.in.StartRoundUseCase;
import dev.enzo.blackjack.game.domain.exception.InvalidRoundStateException;
import dev.enzo.blackjack.game.domain.model.*;
import dev.enzo.blackjack.player.application.exception.PlayerNotFoundException;
import dev.enzo.blackjack.shared.infrastructure.in.web.GlobalValidationExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.List;

import static org.mockito.Mockito.*;

class RoundControllerTest {

    @Test
    void shouldStartRoundAndReturn201() {
        UUID playerId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase useCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(useCase.startRound(playerId)).thenReturn(Mono.just(roundId));

        WebTestClient client = WebTestClient.bindToController(new RoundController(useCase, hitUseCase, standUseCase,
                        getRoundUseCase)).build();

        client.post().uri("/api/rounds").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "playerId": "%s"
                        }
                        """.formatted(playerId))
                .exchange().expectStatus().isCreated().expectBody(String.class)
                .isEqualTo("\"" + roundId + "\"");

        verify(useCase).startRound(playerId);
    }

    @Test
    void shouldReturn400WhenPlayerIdIsNull() {
        StartRoundUseCase useCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        WebTestClient client =
                WebTestClient.bindToController(new RoundController(useCase, hitUseCase, standUseCase, getRoundUseCase))
                        .controllerAdvice(new RoundExceptionHandler(), new GlobalValidationExceptionHandler()).build();

        client.post().uri("/api/rounds").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "playerId": null
                    }
                    """)
                .exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.error")
                .isEqualTo("Player ID is required");

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturn400WhenPlayerIdIsInvalid() {
        StartRoundUseCase useCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        WebTestClient client = WebTestClient.bindToController(new RoundController(useCase, hitUseCase, standUseCase,
                        getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "playerId": "abc123"
                    }
                    """)
                .exchange().expectStatus().isBadRequest();

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturn404WhenPlayerDoesNotExist() {
        UUID playerId = UUID.randomUUID();

        StartRoundUseCase useCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(useCase.startRound(playerId)).thenReturn(Mono.error(new PlayerNotFoundException(playerId)));

        WebTestClient client = WebTestClient.bindToController(new RoundController(useCase, hitUseCase, standUseCase,
                        getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "playerId": "%s"
                    }
                    """.formatted(playerId))
                .exchange().expectStatus().isNotFound().expectBody().jsonPath("$.error")
                .isEqualTo("Player not found: " + playerId);

        verify(useCase).startRound(playerId);
    }

    @Test
    void shouldHitRoundAndReturn204() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(hitUseCase.hit(roundId)).thenReturn(Mono.empty());

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).build();

        client.post().uri("/api/rounds/{roundId}/hit", roundId).exchange().expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(hitUseCase).hit(roundId);
        verifyNoInteractions(startUseCase, standUseCase, getRoundUseCase);
    }

    @Test
    void shouldReturn404WhenHittingNonExistingRound() {
        UUID roundId = UUID.randomUUID();
        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(hitUseCase.hit(roundId)).thenReturn(Mono.error(new RoundNotFoundException(roundId)));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                        standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds/{roundId}/hit", roundId).exchange().expectStatus().isNotFound().expectBody()
                .jsonPath("$.error").isEqualTo("Round not found: " + roundId);

        verify(hitUseCase).hit(roundId);
        verifyNoInteractions(startUseCase, standUseCase, getRoundUseCase);
    }

    @Test
    void shouldReturn409WhenHittingFinishedRound() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(hitUseCase.hit(roundId))
                .thenReturn(Mono.error(new InvalidRoundStateException("It's not the player's turn!")));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                        standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds/{roundId}/hit", roundId).exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.error").isEqualTo("It's not the player's turn!");

        verify(hitUseCase).hit(roundId);
        verifyNoInteractions(startUseCase, standUseCase, getRoundUseCase);
    }

    @Test
    void shouldReturn409WhenHittingWith21Points() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(hitUseCase.hit(roundId)).thenReturn(Mono.error(new InvalidRoundStateException("Cannot hit with 21")));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                        standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds/{roundId}/hit", roundId).exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.error").isEqualTo("Cannot hit with 21");

        verify(hitUseCase).hit(roundId);
        verifyNoInteractions(startUseCase, standUseCase, getRoundUseCase);
    }

    @Test
    void shouldStandRoundAndReturn204() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(standUseCase.stand(roundId)).thenReturn(Mono.empty());

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).build();

        client.post().uri("/api/rounds/{roundId}/stand", roundId).exchange().expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(standUseCase).stand(roundId);
        verifyNoInteractions(startUseCase, hitUseCase, getRoundUseCase);
    }

    @Test
    void shouldReturn404WhenStandingNonExistingRound() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(standUseCase.stand(roundId)).thenReturn(Mono.error(new RoundNotFoundException(roundId)));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds/{roundId}/stand", roundId).exchange().expectStatus().isNotFound()
                .expectBody().jsonPath("$.error").isEqualTo("Round not found: " + roundId);

        verify(standUseCase).stand(roundId);
        verifyNoInteractions(startUseCase, hitUseCase, getRoundUseCase);
    }

    @Test
    void shouldReturn409WhenStandingFinishedRound() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(standUseCase.stand(roundId))
                .thenReturn(Mono.error(new InvalidRoundStateException("It's not the player's turn!")));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post().uri("/api/rounds/{roundId}/stand", roundId).exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.error").isEqualTo("It's not the player's turn!");

        verify(standUseCase).stand(roundId);
        verifyNoInteractions(startUseCase, hitUseCase, getRoundUseCase);
    }

    @Test
    void shouldGetRoundAndReturn200() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        Round round = mock(Round.class);

        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getStatus()).thenReturn(RoundStatus.PLAYER_TURN);

        when(round.getPlayerHand()).thenReturn(new HandSnapshot(List.of(new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.SPADES, Rank.EIGHT))));

        when(round.getDealerHand()).thenReturn(new HandSnapshot(List.of(new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.SEVEN))));

        when(getRoundUseCase.getRound(roundId)).thenReturn(Mono.just(round));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).build();

        client.get().uri("/api/rounds/{roundId}", roundId).exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.roundId").isEqualTo(roundId.toString()).jsonPath("$.playerId")
                .isEqualTo(playerId.toString()).jsonPath("$.status").isEqualTo("PLAYER_TURN")
                .jsonPath("$.playerHand.score").isEqualTo(18)
                .jsonPath("$.dealerHand.score").isEqualTo(10)
                .jsonPath("$.dealerHand.cards.length()").isEqualTo(1)
                .jsonPath("$.result").isEmpty();

        verify(getRoundUseCase).getRound(roundId);
        verifyNoInteractions(startUseCase, hitUseCase, standUseCase);
    }

    @Test
    void shouldReturn404WhenGettingNonExistingRound() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(getRoundUseCase.getRound(roundId)).thenReturn(Mono.error(new RoundNotFoundException(roundId)));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.get().uri("/api/rounds/{roundId}", roundId).exchange().expectStatus().isNotFound().expectBody()
                .jsonPath("$.error").isEqualTo("Round not found: " + roundId);

        verify(getRoundUseCase).getRound(roundId);
        verifyNoInteractions(startUseCase, hitUseCase, standUseCase);
    }

    @Test
    void shouldGetFinishedRoundAndRevealDealerCards() {
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        Round round = mock(Round.class);

        when(round.getRoundId()).thenReturn(roundId);
        when(round.getPlayerId()).thenReturn(playerId);
        when(round.getStatus()).thenReturn(RoundStatus.FINISHED);
        when(round.getResult()).thenReturn(RoundResult.PLAYER_WIN);

        when(round.getPlayerHand()).thenReturn(new HandSnapshot(List.of(new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.SPADES, Rank.EIGHT))));

        when(round.getDealerHand()).thenReturn(new HandSnapshot(List.of(new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.SEVEN))));

        when(getRoundUseCase.getRound(roundId)).thenReturn(Mono.just(round));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).build();

        client.get().uri("/api/rounds/{roundId}", roundId).exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.status").isEqualTo("FINISHED")
                .jsonPath("$.playerHand.score").isEqualTo(18)
                .jsonPath("$.dealerHand.score").isEqualTo(17)
                .jsonPath("$.dealerHand.cards.length()").isEqualTo(2)
                .jsonPath("$.result").isEqualTo("PLAYER_WIN");

        verify(getRoundUseCase).getRound(roundId);
        verifyNoInteractions(startUseCase, hitUseCase, standUseCase);
    }

    @Test
    void shouldReturn500WhenUnexpectedIllegalStateExceptionOccurs() {
        UUID roundId = UUID.randomUUID();

        StartRoundUseCase startUseCase = mock(StartRoundUseCase.class);
        HitRoundUseCase hitUseCase = mock(HitRoundUseCase.class);
        StandRoundUseCase standUseCase = mock(StandRoundUseCase.class);
        GetRoundUseCase getRoundUseCase = mock(GetRoundUseCase.class);

        when(hitUseCase.hit(roundId)).thenReturn(Mono.error(new IllegalStateException("Unexpected internal error")));

        WebTestClient client = WebTestClient.bindToController(new RoundController(startUseCase, hitUseCase,
                standUseCase, getRoundUseCase)).controllerAdvice(new RoundExceptionHandler()).build();

        client.post()
                .uri("/api/rounds/{roundId}/hit", roundId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(hitUseCase).hit(roundId);
    }

}
