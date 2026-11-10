package dev.enzo.blackjack.ranking.integration;

import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import dev.enzo.blackjack.game.domain.model.RoundResult;
import dev.enzo.blackjack.player.application.usecase.CreatePlayerService;
import dev.enzo.blackjack.player.application.usecase.RecordRoundResultService;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryPlayerRepository;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryRoundResultRepository;
import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import dev.enzo.blackjack.ranking.application.usecase.GetRankingService;
import dev.enzo.blackjack.ranking.infrastructure.fake.InMemoryRankingRepository;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankingFlowIntegrationTest {

    @Test
    void shouldUpdateRankingAfterRecordingRoundResults() {
        InMemoryPlayerRepository playerRepository = new InMemoryPlayerRepository();
        InMemoryRoundResultRepository resultRepository = new InMemoryRoundResultRepository(playerRepository);
        InMemoryRankingRepository rankingRepository = new InMemoryRankingRepository(playerRepository);
        CreatePlayerService createPlayerService = new CreatePlayerService(playerRepository);
        RecordRoundResultService recordResultService = new RecordRoundResultService(resultRepository);
        GetRankingService getRankingService = new GetRankingService(rankingRepository);

        Mono<List<RankingEntry>> flow = createPlayerService.createPlayer("Enzo").flatMap(enzo ->
                        createPlayerService.createPlayer("Carla").flatMap(carla ->
                                        createPlayerService.createPlayer("Denise")
                                                .flatMap(denise -> {
                                                    UUID enzoId = enzo.playerId();
                                                    UUID carlaId = carla.playerId();
                                                    UUID deniseId = denise.playerId();

                                                    return Flux.concat(recordResultService.recordResult(
                                                            new RoundFinishedEvent(UUID.randomUUID(), enzoId,
                                                                    RoundResult.PLAYER_WIN)),
                                                                    recordResultService.recordResult(
                                                                            new RoundFinishedEvent(UUID.randomUUID(),
                                                                                    enzoId, RoundResult.PLAYER_WIN)),
                                                                    recordResultService.recordResult(
                                                                            new RoundFinishedEvent(UUID.randomUUID(),
                                                                                    carlaId, RoundResult.PLAYER_WIN)),
                                                                    recordResultService.recordResult(
                                                                            new RoundFinishedEvent(UUID.randomUUID(),
                                                                                    deniseId, RoundResult.PLAYER_WIN)),
                                                                    recordResultService.recordResult(
                                                                            new RoundFinishedEvent(UUID.randomUUID(),
                                                                                    deniseId, RoundResult.DEALER_WIN)))
                                                            .then(getRankingService.getRanking().collectList())
                                                            .doOnNext(ranking -> {

                                                                assertEquals(3, ranking.size());
                                                                assertEquals(enzoId, ranking.get(0).playerId());

                                                                assertEquals(carlaId, ranking.get(1).playerId());
                                                                assertEquals(deniseId, ranking.get(2).playerId());

                                                                assertAll(
                                                                        () -> assertEquals(
                                                                                2, ranking.getFirst().wins()),
                                                                        () -> assertEquals(
                                                                                0, ranking.getFirst().losses()),
                                                                        () -> assertEquals(
                                                                                1, ranking.get(1).wins()),
                                                                        () -> assertEquals(
                                                                                0, ranking.get(1).losses()),
                                                                        () -> assertEquals(
                                                                                1, ranking.get(2).wins()),
                                                                        () -> assertEquals(
                                                                                1, ranking.get(2).losses()));
                                                            });
                                                })));

        StepVerifier.create(flow).expectNextCount(1).verifyComplete();
    }

    @Test
    void shouldNotChangeRankingWhenRoundResultIsProcessedTwice() {
        InMemoryPlayerRepository playerRepository = new InMemoryPlayerRepository();
        InMemoryRoundResultRepository resultRepository = new InMemoryRoundResultRepository(playerRepository);
        InMemoryRankingRepository rankingRepository = new InMemoryRankingRepository(playerRepository);
        CreatePlayerService createPlayerService = new CreatePlayerService(playerRepository);
        RecordRoundResultService recordResultService = new RecordRoundResultService(resultRepository);
        GetRankingService getRankingService = new GetRankingService(rankingRepository);

        Mono<List<List<RankingEntry>>> flow = createPlayerService.createPlayer("Enzo")
                .flatMap(enzo -> createPlayerService.createPlayer("Carla")
                                .flatMap(carla -> {
                                    UUID enzoId = enzo.playerId();
                                    UUID carlaId = carla.playerId();
                                    UUID roundId = UUID.randomUUID();

                                    RoundFinishedEvent event = new RoundFinishedEvent(roundId, enzoId,
                                            RoundResult.PLAYER_WIN);

                                    return recordResultService.recordResult(new RoundFinishedEvent(UUID.randomUUID(),
                                                            carlaId, RoundResult.PLAYER_WIN))
                                            .then(recordResultService.recordResult(event))
                                            .then(getRankingService.getRanking().collectList())
                                            .flatMap(rankingBefore ->
                                                            recordResultService.recordResult(event).then(
                                                                    getRankingService.getRanking().collectList())
                                                                    .map(rankingAfter ->
                                                                            List.of(rankingBefore, rankingAfter)));
                                }));

        StepVerifier.create(flow).assertNext(rankings -> {
            List<RankingEntry> rankingBefore = rankings.get(0);
            List<RankingEntry> rankingAfter = rankings.get(1);

            assertEquals(rankingBefore, rankingAfter);

            assertEquals(1, rankingAfter.stream().filter(entry ->
                                    entry.playerName().equals("Enzo")).findFirst().orElseThrow().wins());

                    assertEquals(1, rankingAfter.stream().filter(entry ->
                                            entry.playerName().equals("Carla")).findFirst().orElseThrow().wins());
        }).verifyComplete();
    }

}
