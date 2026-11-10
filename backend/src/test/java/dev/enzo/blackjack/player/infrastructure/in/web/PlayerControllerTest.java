package dev.enzo.blackjack.player.infrastructure.in.web;

import dev.enzo.blackjack.player.application.dto.CreatePlayerResult;
import dev.enzo.blackjack.player.application.port.in.CreatePlayerUseCase;
import dev.enzo.blackjack.shared.infrastructure.in.web.GlobalValidationExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.*;

class PlayerControllerTest {

    @Test
    void shouldCreatePlayerAndReturn201() {
        UUID playerId = UUID.randomUUID();
        CreatePlayerUseCase useCase = mock(CreatePlayerUseCase.class);

        when(useCase.createPlayer("Enzo"))
                .thenReturn(Mono.just(new CreatePlayerResult(playerId, "Enzo")));

        WebTestClient client = WebTestClient.bindToController(new PlayerController(useCase)).build();

        client.post().uri("/api/players").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Enzo"
                        }
                        """)
                .exchange().expectStatus().isCreated().expectBody()
                .jsonPath("$.playerId").isEqualTo(playerId.toString())
                .jsonPath("$.name").isEqualTo("Enzo");

        verify(useCase).createPlayer("Enzo");
    }

    @Test
    void shouldReturn400WhenPlayerNameIsBlank() {
        CreatePlayerUseCase useCase = mock(CreatePlayerUseCase.class);

        WebTestClient client = WebTestClient.bindToController(new PlayerController(useCase))
                .controllerAdvice(new PlayerExceptionHandler(), new GlobalValidationExceptionHandler()).build();

        client.post().uri("/api/players").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "name": ""
                    }
                    """)
                .exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.error")
                .isEqualTo("Player name cannot be blank");

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturn400WhenPlayerNameIsNull() {
        CreatePlayerUseCase useCase = mock(CreatePlayerUseCase.class);

        WebTestClient client = WebTestClient.bindToController(new PlayerController(useCase))
                .controllerAdvice(new PlayerExceptionHandler(), new GlobalValidationExceptionHandler()).build();

        client.post().uri("/api/players").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "name": null
                    }
                    """)
                .exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.error")
                .isEqualTo("Player name cannot be blank");

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturn400WhenPlayerNameContainsOnlySpaces() {
        CreatePlayerUseCase useCase = mock(CreatePlayerUseCase.class);

        WebTestClient client = WebTestClient.bindToController(new PlayerController(useCase))
                .controllerAdvice(new PlayerExceptionHandler(), new GlobalValidationExceptionHandler()).build();

        client.post().uri("/api/players").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "name": "   "
                    }
                    """)
                .exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.error")
                .isEqualTo("Player name cannot be blank");

        verifyNoInteractions(useCase);
    }

}
