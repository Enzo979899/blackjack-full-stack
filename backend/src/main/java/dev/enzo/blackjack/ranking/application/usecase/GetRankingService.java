package dev.enzo.blackjack.ranking.application.usecase;

import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import dev.enzo.blackjack.ranking.application.port.in.GetRankingUseCase;
import dev.enzo.blackjack.ranking.application.port.out.RankingRepository;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GetRankingService implements GetRankingUseCase {

    private final RankingRepository rankingRepository;

    public GetRankingService(RankingRepository rankingRepository) {
        this.rankingRepository = Objects.requireNonNull(rankingRepository);
    }

    @Override
    public Flux<RankingEntry> getRanking() {
        return rankingRepository.findAll().sort(Comparator.comparingInt(RankingEntry::wins).reversed()
                .thenComparingInt(RankingEntry::losses)
                .thenComparing(RankingEntry::playerName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RankingEntry::playerId));
    }
}
