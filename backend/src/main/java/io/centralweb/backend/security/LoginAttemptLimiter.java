package io.centralweb.backend.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class LoginAttemptLimiter {
    private static final DefaultRedisScript<Long> RESERVE = new DefaultRedisScript<>("""
            local clock = redis.call('TIME')
            local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
            local window = 900000
            for i = 1, 2 do
                redis.call('ZREMRANGEBYSCORE', KEYS[i], '-inf', now - window)
            end
            local wait = 0
            local limits = {5, 30}
            for i = 1, 2 do
                if redis.call('ZCARD', KEYS[i]) >= limits[i] then
                    local oldest = redis.call('ZRANGE', KEYS[i], 0, 0, 'WITHSCORES')
                    wait = math.max(wait, tonumber(oldest[2]) + window - now)
                end
            end
            if wait > 0 then return wait end
            for i = 1, 2 do
                redis.call('ZADD', KEYS[i], now, ARGV[1])
                redis.call('PEXPIRE', KEYS[i], window)
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;

    public LoginAttemptLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Reservation reserve(String email, String ip) {
        String accountKey = "rate-limit:login:account:" + hash(email);
        String ipKey = "rate-limit:login:ip:" + ip;
        String attemptId = UUID.randomUUID().toString();
        Long wait = redis.execute(RESERVE, List.of(accountKey, ipKey), attemptId);
        if (wait == null) {
            throw new IllegalStateException("Não foi possível verificar o limite de login");
        }
        return new Reservation(accountKey, attemptId, wait);
    }

    public void completeSuccessfulLogin(Reservation reservation) {
        redis.opsForZSet().remove(reservation.accountKey(), reservation.attemptId());
    }

    private String hash(String email) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(email.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível", exception);
        }
    }

    public record Reservation(String accountKey, String attemptId, long waitMillis) {}
}
