package dev.enzo.blackjack.shared.configuration;

import dev.enzo.blackjack.game.application.port.out.RoundRepository;
import dev.enzo.blackjack.game.infrastructure.fake.InMemoryRoundRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration"
})
@ActiveProfiles("memory")
class RoundRepositoryConfigurationTest {

    @Autowired
    private RoundRepository roundRepository;

    @Test
    void shouldUseInMemoryRepositoryWhenMongoProfileIsInactive() {
        assertInstanceOf(InMemoryRoundRepository.class, roundRepository);
    }
}
