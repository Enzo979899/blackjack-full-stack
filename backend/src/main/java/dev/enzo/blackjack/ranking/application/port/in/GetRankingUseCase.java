package dev.enzo.blackjack.ranking.application.port.in;

import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import reactor.core.publisher.Flux;


public interface GetRankingUseCase {

    Flux<RankingEntry> getRanking();

}
