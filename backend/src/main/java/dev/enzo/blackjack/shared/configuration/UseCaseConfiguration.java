package dev.enzo.blackjack.shared.configuration;

import dev.enzo.blackjack.game.application.port.out.DeckFactory;
import dev.enzo.blackjack.game.application.port.out.RoundEventPublisher;
import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.application.usecase.GetRoundService;
import dev.enzo.blackjack.game.application.usecase.HitRoundService;
import dev.enzo.blackjack.game.application.usecase.StandRoundService;
import dev.enzo.blackjack.game.application.usecase.StartRoundService;
import dev.enzo.blackjack.game.infrastructure.factory.DefaultDeckFactory;
import dev.enzo.blackjack.game.infrastructure.fake.InMemoryRoundEventPublisher;
import dev.enzo.blackjack.game.infrastructure.fake.InMemoryRoundRepository;
import dev.enzo.blackjack.player.application.port.in.RecordRoundResultUseCase;
import dev.enzo.blackjack.player.application.port.out.PlayerRepository;
import dev.enzo.blackjack.player.application.port.out.RoundResultRepository;
import dev.enzo.blackjack.player.application.usecase.CreatePlayerService;
import dev.enzo.blackjack.player.application.usecase.RecordRoundResultService;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryPlayerRepository;
import dev.enzo.blackjack.player.infrastructure.fake.InMemoryRoundResultRepository;
import dev.enzo.blackjack.ranking.application.port.out.RankingRepository;
import dev.enzo.blackjack.ranking.application.usecase.GetRankingService;
import dev.enzo.blackjack.ranking.infrastructure.fake.InMemoryRankingRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public CreatePlayerService createPlayerService(PlayerRepository playerRepository) {
        return new CreatePlayerService(playerRepository);
    }

    @Bean
    @Profile("!mysql")
    public PlayerRepository playerRepository() {
        return new InMemoryPlayerRepository();
    }

    @Bean
    public RankingRepository rankingRepository(PlayerRepository playerRepository) {
        return new InMemoryRankingRepository(playerRepository);
    }

    @Bean
    public GetRankingService getRankingService(RankingRepository rankingRepository) {
        return new GetRankingService(rankingRepository);
    }

    @Bean
    @Profile("!mysql")
    public RoundResultRepository roundResultRepository(PlayerRepository playerRepository) {
        return new InMemoryRoundResultRepository(playerRepository);
    }

    @Bean
    public RecordRoundResultService recordRoundResultService(RoundResultRepository roundResultRepository) {
        return new RecordRoundResultService(roundResultRepository);
    }

    @Bean
    public DeckFactory deckFactory() {
        return new DefaultDeckFactory();
    }

    @Bean
    @Profile("!mongo")
    public RoundRepository roundRepository() {
        return new InMemoryRoundRepository();
    }

    @Bean
    public RoundEventPublisher roundEventPublisher(RecordRoundResultUseCase recordRoundResultUseCase) {
        return new InMemoryRoundEventPublisher(recordRoundResultUseCase);
    }

    @Bean
    public StartRoundService startRoundService(PlayerRepository playerRepository, RoundRepository roundRepository,
                                               RoundEventPublisher eventPublisher, DeckFactory deckFactory) {
        return new StartRoundService(playerRepository, roundRepository, eventPublisher, deckFactory);
    }

    @Bean
    public HitRoundService hitRoundService(RoundRepository roundRepository, RoundEventPublisher roundEventPublisher) {
        return new HitRoundService(roundRepository, roundEventPublisher);
    }

    @Bean
    public StandRoundService standRoundService(RoundRepository roundRepository,
                                               RoundEventPublisher roundEventPublisher) {
        return new StandRoundService(roundRepository, roundEventPublisher);
    }

    @Bean
    public GetRoundService getRoundService(RoundRepository roundRepository) {
        return new GetRoundService(roundRepository);
    }

}
