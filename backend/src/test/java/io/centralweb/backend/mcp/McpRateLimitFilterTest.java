package io.centralweb.backend.mcp;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class McpRateLimitFilterTest {
    private final TokenBucketRateLimiter redis = mock(TokenBucketRateLimiter.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final McpRateLimitFilter filter = new McpRateLimitFilter(redis, mapper);
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
        when(redis.consume(eq("bucket4j:mcp:192.0.2.1"), eq(10), eq(Duration.ofSeconds(1))))
                .thenReturn(ConsumptionProbe.consumed(9, 0));
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void exhaustedBucketReturnsRetryAfterWithoutContinuing() throws Exception {
        when(redis.consume(anyString(), anyInt(), any())).thenReturn(ConsumptionProbe.rejected(0, 350_000_000L, 0));
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("1");
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(mapper.readTree(response.getContentAsString()).path("code").asInt()).isEqualTo(429);
        verifyNoInteractions(chain);
    }

    @Test
    void redisFailureReturnsServiceUnavailable() throws Exception {
        when(redis.consume(anyString(), anyInt(), any()))
                .thenThrow(new DataAccessResourceFailureException("Redis indisponível"));
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(mapper.readTree(response.getContentAsString()).path("code").asInt()).isEqualTo(503);
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
