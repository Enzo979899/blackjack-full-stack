package dev.enzo.blackjack.player.application.usecase;

import dev.enzo.blackjack.player.application.dto.CreatePlayerResult;
import dev.enzo.blackjack.player.application.exception.DuplicatePlayerNameException;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.domain.model.Player;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreatePlayerServiceTest {

    @Test
    void shouldCreateAndSavePlayer() {
        PlayerRepository repository = mock(PlayerRepository.class);

        when(repository.existsByNameIgnoreCase(anyString())).thenReturn(Mono.just(false));

        when(repository.save(any(Player.class))).thenReturn(Mono.empty());

        CreatePlayerService service = new CreatePlayerService(repository);

        StepVerifier.create(service.createPlayer("Enzo")).assertNext(result -> {
            assertNotNull(result.playerId());
            assertEquals("Enzo", result.name());

            ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);

            verify(repository).save(captor.capture());

            Player savedPlayer = captor.getValue();

            assertEquals(result.playerId(), savedPlayer.getId());
            assertEquals(result.name(), savedPlayer.getName());
        }).verifyComplete();
    }

    @Test
    void shouldNotSavePlayerWhenNameIsNull() {
        PlayerRepository repository = mock(PlayerRepository.class);
        CreatePlayerService service = new CreatePlayerService(repository);

        StepVerifier.create(service.createPlayer(null)).expectError(NullPointerException.class).verify();
        verify(repository, never()).save(any(Player.class));
    }

    @Test
    void shouldNotSavePlayerWhenNameIsBlank() {
        PlayerRepository repository = mock(PlayerRepository.class);
        CreatePlayerService service = new CreatePlayerService(repository);

        StepVerifier.create(service.createPlayer("")).expectError(IllegalArgumentException.class).verify();

        StepVerifier.create(service.createPlayer("   ")).expectError(IllegalArgumentException.class).verify();
        verify(repository, never()).save(any(Player.class));
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentPlayers() {
        PlayerRepository repository = mock(PlayerRepository.class);

        when(repository.existsByNameIgnoreCase(anyString())).thenReturn(Mono.just(false));

        when(repository.save(any(Player.class)))
                .thenReturn(Mono.empty());

        CreatePlayerService service = new CreatePlayerService(repository);

        Flux<CreatePlayerResult> players = Flux.concat(service.createPlayer("Enzo"),
                service.createPlayer("Carla"));

        StepVerifier.create(players).recordWith(java.util.ArrayList::new).expectNextCount(2)
                .consumeRecordedWith(results -> {
                    var iterator = results.iterator();
                    CreatePlayerResult firstPlayer = iterator.next();
                    CreatePlayerResult secondPlayer = iterator.next();

                    assertNotNull(firstPlayer.playerId());
                    assertNotNull(secondPlayer.playerId());

                    assertNotEquals(firstPlayer.playerId(), secondPlayer.playerId());
                    assertEquals("Enzo", firstPlayer.name());
                    assertEquals("Carla", secondPlayer.name());
                }).verifyComplete();

        verify(repository, times(2)).save(any(Player.class));
    }

    @Test
    void shouldNotCreatePlayerWhenNameAlreadyExists() {
        PlayerRepository repository = mock(PlayerRepository.class);

        when(repository.existsByNameIgnoreCase("Enzo"))
                .thenReturn(Mono.just(true));

        CreatePlayerService service = new CreatePlayerService(repository);

        StepVerifier.create(service.createPlayer("Enzo"))
                .expectErrorMatches(error ->
                        error instanceof DuplicatePlayerNameException && error.getMessage()
                                .equals("Player name already exists")).verify();

        verify(repository).existsByNameIgnoreCase("Enzo");
        verify(repository, never()).save(any(Player.class));
    }

    @Test
    void shouldRejectDuplicateNameIgnoringCaseAndSpaces() {
        PlayerRepository repository = mock(PlayerRepository.class);

        when(repository.existsByNameIgnoreCase("enzo")).thenReturn(Mono.just(true));

        CreatePlayerService service = new CreatePlayerService(repository);

        StepVerifier.create(service.createPlayer("  enzo  "))
                .expectError(DuplicatePlayerNameException.class).verify();

        verify(repository).existsByNameIgnoreCase("enzo");
        verify(repository, never()).save(any(Player.class));
    }


}
