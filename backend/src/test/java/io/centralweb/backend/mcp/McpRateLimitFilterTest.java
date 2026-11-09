package io.centralweb.backend.mcp;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class McpRateLimitFilterTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final McpRateLimitFilter filter = new McpRateLimitFilter(redis);
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("POST", "/mcp");
        request.setRemoteAddr("192.0.2.1");
        response = new MockHttpServletResponse();
    }

    @Test
    void allowedRequestContinuesWithIpSpecificKey() throws Exception {
        when(redis.execute(any(RedisScript.class), eq(List.of("rate-limit:mcp:192.0.2.1"))))
                .thenReturn(0L);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void exhaustedBucketReturnsRetryAfterWithoutContinuing() throws Exception {
        when(redis.execute(any(RedisScript.class), anyList())).thenReturn(350L);
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("1");
        verifyNoInteractions(chain);
    }

    @Test
    void redisFailureReturnsServiceUnavailable() throws Exception {
        when(redis.execute(any(RedisScript.class), anyList()))
                .thenThrow(new DataAccessResourceFailureException("Redis indisponível"));
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(503);
        verifyNoInteractions(chain);
    }

    @Test
    void otherEndpointsBypassRateLimit() throws Exception {
        request.setRequestURI("/articles");
        filter.doFilter(request, response, chain);
        verifyNoInteractions(redis);
        verify(chain).doFilter(request, response);
    }
}
