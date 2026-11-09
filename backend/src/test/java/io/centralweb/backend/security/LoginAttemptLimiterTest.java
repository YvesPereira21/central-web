package io.centralweb.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginAttemptLimiterTest {
    @Test
    void successfulLoginRemovesOnlyItsAccountReservation() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ZSetOperations<String, String> sets = mock(ZSetOperations.class);
        when(redis.opsForZSet()).thenReturn(sets);
        when(redis.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(0L);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(redis);
        var reservation = limiter.reserve("member@example.com", "192.0.2.1");
        assertThat(reservation.waitMillis()).isZero();
        assertThat(reservation.accountKey()).doesNotContain("member@example.com");
        limiter.completeSuccessfulLogin(reservation);
        verify(sets).remove(reservation.accountKey(), reservation.attemptId());
    }
}
