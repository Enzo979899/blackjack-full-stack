package dev.enzo.blackjack.game.infrastructure.persistence.mongo;

import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.domain.model.Round;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("mongo")
public class MongoRoundRepositoryAdapter implements RoundRepository {
    private final SpringDataRoundRepository springRepository;

    public MongoRoundRepositoryAdapter(SpringDataRoundRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Mono<Void> save(Round round) {
        return Mono.defer(() -> {
            RoundDocument document = RoundDocumentMapper.toDocument(round);

            return springRepository.save(document).then();
        });
    }

    @Override
    public Mono<Round> findById(UUID roundId) {
        return springRepository.findById(roundId.toString()).map(RoundDocumentMapper::toDomain);
    }
}
