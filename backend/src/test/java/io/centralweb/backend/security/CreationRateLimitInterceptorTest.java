package io.centralweb.backend.security;

import io.centralweb.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
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
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString())).thenReturn(1200L);

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("2");
        verify(redis).execute(any(RedisScript.class), eq(List.of("rate-limit:create:answers:user:" + userId)), eq("10"), eq("30000"));
    }

    @Test
    void registrationUsesIpAndOtherMethodsBypassLimiter() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/profiles");
        request.setRemoteAddr("192.0.2.1");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/profiles");
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString())).thenReturn(0L);
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verify(redis).execute(any(RedisScript.class), eq(List.of("rate-limit:create:profiles:ip:192.0.2.1")), eq("3"), eq("600000"));
        reset(redis);
        request.setMethod("GET");
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verifyNoInteractions(redis);
    }
}
