package io.centralweb.backend.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class TokenBucketRateLimiter {
    private final LettuceConnectionFactory connectionFactory;
    private volatile ProxyManager<byte[]> manager;
    private StatefulRedisConnection<byte[], byte[]> connection;

    public TokenBucketRateLimiter(LettuceConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public ConsumptionProbe consume(String key, int capacity, Duration refillPeriod) {
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(capacity).refillGreedy(1, refillPeriod))
                .build();
        return manager().getProxy(key.getBytes(StandardCharsets.UTF_8), () -> configuration)
                .tryConsumeAndReturnRemaining(1);
    }

    private synchronized ProxyManager<byte[]> manager() {
        if (manager == null) {
            RedisClient client = (RedisClient) connectionFactory.getRequiredNativeClient();
            connection = client.connect(ByteArrayCodec.INSTANCE);
            manager = Bucket4jLettuce.casBasedBuilder(connection)
                    .requestTimeout(Duration.ofMillis(500))
                    .expirationAfterWrite(ExpirationAfterWriteStrategy
                            .basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
                    .build();
        }
        return manager;
    }

    @PreDestroy
    public synchronized void close() {
        if (connection != null) {
            connection.close();
        }
    }
}
