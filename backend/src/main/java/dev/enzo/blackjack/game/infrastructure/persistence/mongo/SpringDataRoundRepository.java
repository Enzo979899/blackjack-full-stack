package dev.enzo.blackjack.game.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

interface SpringDataRoundRepository extends ReactiveMongoRepository<RoundDocument, String> {

}
