package dev.enzo.blackjack;

import dev.enzo.blackjack.game.infrastructure.persistence.mongo.RoundDocument;
import dev.enzo.blackjack.player.infrastructure.persistence.mysql.SpringDataPlayerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.mongodb.uri=mongodb://localhost:27017/blackjack_test",
                "spring.r2dbc.url=r2dbc:mysql://localhost:3307/blackjack_test"
        }
)
@ActiveProfiles({"mongo", "mysql"})
class BlackjackIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    private SpringDataPlayerRepository playerRepository;

    @Autowired
    private DatabaseClient databaseClient;

    private UUID createdPlayerId;
    private UUID createdRoundId;

    @AfterEach
    void cleanUp() {
        if (createdRoundId != null) {
            mongoTemplate.remove(org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id")
                            .is(createdRoundId.toString())), RoundDocument.class).block();
        }

        if (createdPlayerId != null) {
            databaseClient.sql("DELETE FROM round_results WHERE player_id = :playerId")
                    .bind("playerId", createdPlayerId.toString()).fetch().rowsUpdated().block();

            playerRepository.deleteById(createdPlayerId.toString()).block();
        }
    }

    @Test
    void shouldPlayCompleteRoundAndUpdateRanking() {

        WebTestClient client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(15)).build();

        String playerName = "Integration-" + UUID.randomUUID();

        Map<?, ?> player = client.post()
                .uri("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", playerName))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(player).isNotNull();

        createdPlayerId = UUID.fromString(player.get("playerId").toString());

        String roundIdResponse = client.post()
                .uri("/api/rounds")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("playerId", createdPlayerId.toString()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(roundIdResponse).isNotNull();

        createdRoundId = UUID.fromString(roundIdResponse.replace("\"", ""));

        Map<?, ?> round = getRound(client);

        assertThat(round.get("roundId")).isEqualTo(createdRoundId.toString());

        assertThat(round.get("playerId")).isEqualTo(createdPlayerId.toString());

        if (!"FINISHED".equals(round.get("status"))) {

            Map<?, ?> dealerHand = (Map<?, ?>) round.get("dealerHand");
            List<?> cards = (List<?>) dealerHand.get("cards");

            assertThat(cards).hasSize(1);
            assertThat(round.get("result")).isNull();

            client.post().uri("/api/rounds/{id}/stand", createdRoundId).exchange().expectStatus().isNoContent();
        }

        Map<?, ?> finishedRound = getRound(client);

        assertThat(finishedRound.get("status")).isEqualTo("FINISHED");
        assertThat(finishedRound.get("result")).isNotNull();

        Map<?, ?> dealerHand = (Map<?, ?>) finishedRound.get("dealerHand");

        assertThat((List<?>) dealerHand.get("cards")).hasSizeGreaterThanOrEqualTo(2);

        Boolean rankingUpdated = Mono.defer(() -> Mono.fromCallable(() -> {

            List<?> ranking = client.get()
                    .uri("/api/ranking")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(List.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(ranking).isNotNull();

            return ranking.stream()
                    .map(item -> (Map<?, ?>) item)
                    .anyMatch(entry -> {
                        if (!createdPlayerId.toString()
                                .equals(entry.get("playerId"))) {
                            return false;
                        }

                        int wins = ((Number) entry.get("wins")).intValue();
                        int losses = ((Number) entry.get("losses")).intValue();
                        int draws = ((Number) entry.get("draws")).intValue();

                        return wins + losses + draws == 1;
                    });
                }))
                .filter(Boolean::booleanValue)
                .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofMillis(250)))
                .blockOptional(Duration.ofSeconds(5))
                .orElse(false);

        assertThat(rankingUpdated).as("The completed round must update the MySQL ranking").isTrue();
    }

    private Map<?, ?> getRound(WebTestClient client) {

        Map<?, ?> response = client.get()
                .uri("/api/rounds/{id}", createdRoundId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();

        return response;
    }


}
