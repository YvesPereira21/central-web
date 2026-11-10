package io.centralweb.backend.ratelimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.time.Duration;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "CENTRAL_TEST_REDIS_PORT", matches = "[0-9]+")
class TokenBucketRedisIntegrationTest {
    @Test
    void instancesShareTokensAndSeparateKeys() {
        var config = new RedisStandaloneConfiguration("127.0.0.1", Integer.parseInt(System.getenv("CENTRAL_TEST_REDIS_PORT")));
        var factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        factory.start();
        try {
            var first = new TokenBucketRateLimiter(factory);
            var second = new TokenBucketRateLimiter(factory);
            String key = "test:bucket4j:" + UUID.randomUUID();
            assertThat(first.consume(key, 2, Duration.ofHours(1)).isConsumed()).isTrue();
            assertThat(second.consume(key, 2, Duration.ofHours(1)).isConsumed()).isTrue();
            var rejected = first.consume(key, 2, Duration.ofHours(1));
            assertThat(rejected.isConsumed()).isFalse();
            assertThat(rejected.getNanosToWaitForRefill()).isPositive();
            assertThat(first.consume(key + ":other", 2, Duration.ofHours(1)).isConsumed()).isTrue();
        } finally {
            factory.destroy();
        }
    }
}
