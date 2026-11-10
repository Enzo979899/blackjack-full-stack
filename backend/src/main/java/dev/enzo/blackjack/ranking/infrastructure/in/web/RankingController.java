package dev.enzo.blackjack.ranking.infrastructure.in.web;

import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import dev.enzo.blackjack.ranking.application.port.in.GetRankingUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    private final GetRankingUseCase getRankingUseCase;

    public RankingController(GetRankingUseCase getRankingUseCase) {
        this.getRankingUseCase = getRankingUseCase;
    }

    @GetMapping
    public Flux<RankingEntry> getRanking() {
        return getRankingUseCase.getRanking();
    }
}
