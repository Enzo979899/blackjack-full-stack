
package dev.enzo.blackjack.ranking.application.usecase;

import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import dev.enzo.blackjack.ranking.application.port.out.RankingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetRankingServiceTest {
    private RankingRepository rankingRepository;
    private GetRankingService service;

    @BeforeEach
    void setUp() {
        rankingRepository = mock(RankingRepository.class);
        service = new GetRankingService(rankingRepository);
    }

    private RankingEntry entry(String name, int wins, int losses, int draws) {
        return new RankingEntry(UUID.randomUUID(), name, wins, losses, draws);
    }

    @Test
    void shouldReturnEmptyRankingWhenNoPlayersExist() {
        when(rankingRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(service.getRanking()).verifyComplete();
        verify(rankingRepository).findAll();
    }

    @Test
    void shouldSortPlayersByWinsDescending() {
        RankingEntry enzo = entry("Enzo", 5, 2, 1);
        RankingEntry carla = entry("Carla", 15, 3, 0);
        RankingEntry denise = entry("Denise", 10, 1, 2);

        when(rankingRepository.findAll()).thenReturn(Flux.just(enzo, carla, denise));

        StepVerifier.create(service.getRanking()).expectNext(carla, denise, enzo).verifyComplete();
    }

    @Test
    void shouldSortPlayersByLossesAscendingWhenWinsAreEqual() {
        RankingEntry enzo = entry("Enzo", 10, 5, 0);
        RankingEntry carla = entry("Carla", 10, 2, 1);
        RankingEntry denise = entry("Denise", 10, 8, 0);

        when(rankingRepository.findAll()).thenReturn(Flux.just(enzo, denise, carla));

        StepVerifier.create(service.getRanking()).expectNext(carla, enzo, denise).verifyComplete();
    }

    @Test
    void shouldSortAlphabeticallyWhenWinsAndLossesAreEqual() {
        RankingEntry enzo = entry("Enzo", 10, 3, 2);
        RankingEntry carla = entry("Carla", 10, 3, 0);
        RankingEntry denise = entry("Denise", 10, 3, 1);

        when(rankingRepository.findAll()).thenReturn(Flux.just(enzo, denise, carla));

        StepVerifier.create(service.getRanking()).expectNext(carla, denise, enzo).verifyComplete();
    }

    @Test
    void shouldSortNamesIgnoringCase() {
        RankingEntry enzo = entry("enzo", 5, 2, 0);
        RankingEntry carla = entry("Carla", 5, 2, 0);
        RankingEntry denise = entry("DENISE", 5, 2, 0);

        when(rankingRepository.findAll()).thenReturn(Flux.just(enzo, denise, carla));

        StepVerifier.create(service.getRanking()).expectNext(carla, denise, enzo).verifyComplete();
    }

    @Test
    void shouldNotModifyOriginalRepositoryList() {
        RankingEntry enzo = entry("Enzo", 5, 2, 0);
        RankingEntry carla = entry("Carla", 10, 1, 0);

        List<RankingEntry> original = List.of(enzo, carla);

        when(rankingRepository.findAll()).thenReturn(Flux.fromIterable(original));

        StepVerifier.create(service.getRanking()).expectNext(carla, enzo).verifyComplete();

        assertEquals(List.of(enzo, carla), original);
    }

    @Test
    void shouldRejectNullRepository() {
        assertThrows(NullPointerException.class, () -> new GetRankingService(null));
    }

    @Test
    void shouldCallRepositoryOnlyOnce() {
        when(rankingRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(service.getRanking()).verifyComplete();

        verify(rankingRepository, times(1)).findAll();
        verifyNoMoreInteractions(rankingRepository);
    }
}
