package dev.enzo.blackjack.player.application.port.in;

import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import reactor.core.publisher.Mono;

public interface RecordRoundResultUseCase {

    Mono<Void> recordResult(RoundFinishedEvent event);

}
