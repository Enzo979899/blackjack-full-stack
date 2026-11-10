package dev.enzo.blackjack.ranking.application.port.out;

import dev.enzo.blackjack.ranking.application.dto.RankingEntry;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RankingRepository {

    Flux<RankingEntry> findAll();

}
