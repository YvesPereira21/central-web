package io.centralweb.backend.security;

import io.centralweb.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CreationRateLimitInterceptorTest {
    private final TokenBucketRateLimiter redis = mock(TokenBucketRateLimiter.class);
    private final CreationRateLimitInterceptor interceptor = new CreationRateLimitInterceptor(redis);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void limitsDynamicAnswerRouteByUserAndReturnsRetryAfter() {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getUserId()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/answers/123");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/answers/{questionId}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(redis.consume(anyString(), anyInt(), any())).thenReturn(ConsumptionProbe.rejected(0, 1_200_000_000L, 0));

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("2");
        verify(redis).consume("bucket4j:create:answers:user:" + userId, 10, Duration.ofMillis(30000));
    }

    @Test
    void registrationUsesIpAndOtherMethodsBypassLimiter() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/profiles");
        request.setRemoteAddr("192.0.2.1");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/profiles");
        when(redis.consume(anyString(), anyInt(), any())).thenReturn(ConsumptionProbe.consumed(2, 0));
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verify(redis).consume("bucket4j:create:profiles:ip:192.0.2.1", 3, Duration.ofMillis(600000));
        reset(redis);
        request.setMethod("GET");
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verifyNoInteractions(redis);
    }
}
