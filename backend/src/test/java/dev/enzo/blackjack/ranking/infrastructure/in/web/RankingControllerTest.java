package dev.enzo.blackjack.ranking.infrastructure.in.web;

import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import dev.enzo.blackjack.ranking.application.port.in.GetRankingUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.Mockito.*;

class RankingControllerTest {

    @Test
    void shouldGetRankingAndReturn200() {
        UUID playerId = UUID.randomUUID();
        GetRankingUseCase getRankingUseCase = mock(GetRankingUseCase.class);
        RankingEntry entry = new RankingEntry(playerId, "Enzo", 1, 0, 0);

        when(getRankingUseCase.getRanking()).thenReturn(Flux.just(entry));

        WebTestClient client = WebTestClient.bindToController(new RankingController(getRankingUseCase)).build();

        client.get().uri("/api/ranking").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].playerId").isEqualTo(playerId.toString())
                .jsonPath("$[0].playerName").isEqualTo("Enzo").jsonPath("$[0].wins")
                .isEqualTo(1).jsonPath("$[0].losses").isEqualTo(0)
                .jsonPath("$[0].draws").isEqualTo(0);

        verify(getRankingUseCase).getRanking();
    }

    @Test
    void shouldReturnEmptyRankingWhenNoPlayersExist() {
        GetRankingUseCase getRankingUseCase = mock(GetRankingUseCase.class);

        when(getRankingUseCase.getRanking()).thenReturn(Flux.empty());

        WebTestClient client = WebTestClient.bindToController(new RankingController(getRankingUseCase)).build();

        client.get().uri("/api/ranking").exchange().expectStatus().isOk().expectBody().json("[]");

        verify(getRankingUseCase).getRanking();
    }


}
