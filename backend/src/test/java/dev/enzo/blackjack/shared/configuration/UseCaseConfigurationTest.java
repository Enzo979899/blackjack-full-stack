package dev.enzo.blackjack.shared.configuration;

import dev.enzo.blackjack.game.application.usecase.HitRoundService;
import dev.enzo.blackjack.game.application.usecase.StandRoundService;
import dev.enzo.blackjack.game.application.usecase.StartRoundService;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.application.port.out.RoundResultRepository;
import dev.enzo.blackjack.player.application.usecase.CreatePlayerService;
import dev.enzo.blackjack.player.application.usecase.RecordRoundResultService;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryPlayerRepository;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryRoundResultRepository;
import dev.enzo.blackjack.ranking.application.port.out.RankingRepository;
import dev.enzo.blackjack.ranking.application.usecase.GetRankingService;
import dev.enzo.blackjack.ranking.infrastructure.fake.InMemoryRankingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseConfigurationTest {

    @Test
    void shouldInjectPlayerRepositoryIntoCreatePlayerService() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(UseCaseConfiguration.class)) {

            CreatePlayerService service = context.getBean(CreatePlayerService.class);
            PlayerRepository repository = context.getBean(PlayerRepository.class);

            assertNotNull(service);
            assertNotNull(repository);
            assertInstanceOf(InMemoryPlayerRepository.class, repository);
        }

    }

    @Test
    void shouldInjectRankingRepositoryIntoGetRankingService() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(UseCaseConfiguration.class)) {

            GetRankingService service = context.getBean(GetRankingService.class);
            RankingRepository repository = context.getBean(RankingRepository.class);
            PlayerRepository playerRepository = context.getBean(PlayerRepository.class);

            assertNotNull(service);
            assertInstanceOf(InMemoryRankingRepository.class, repository);

            assertSame(playerRepository, context.getBean(PlayerRepository.class));
        }
    }

    @Test
    void shouldInjectRoundResultRepositoryIntoRecordRoundResultService() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(UseCaseConfiguration.class)) {

            RecordRoundResultService service = context.getBean(RecordRoundResultService.class);

            RoundResultRepository repository = context.getBean(RoundResultRepository.class);

            assertNotNull(service);
            assertNotNull(repository);
            assertInstanceOf(InMemoryRoundResultRepository.class, repository);
        }
    }

    @Test
    void shouldInjectGameServices() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(UseCaseConfiguration.class)) {

            StartRoundService startRoundService = context.getBean(StartRoundService.class);

            HitRoundService hitRoundService = context.getBean(HitRoundService.class);

            StandRoundService standRoundService = context.getBean(StandRoundService.class);

            assertNotNull(startRoundService);
            assertNotNull(hitRoundService);
            assertNotNull(standRoundService);
        }
    }

}
