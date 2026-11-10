package dev.enzo.blackjack.game.application.port.out;

import dev.enzo.blackjack.game.domain.event.RoundFinishedEvent;
import reactor.core.publisher.Mono;

public interface RoundEventPublisher {

    Mono<Void> publish(RoundFinishedEvent event);

}
