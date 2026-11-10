package io.centralweb.backend.security;

import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PublicRateLimitFilterTest {

    private static final String IP = "192.0.2.1";

    private final TokenBucketRateLimiter limiter = mock(TokenBucketRateLimiter.class);
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final PublicRateLimitFilter filter = new PublicRateLimitFilter(limiter, mapper);

    @Test
    void registrationAndRefreshUseSeparateIpQuotas() throws Exception {
        when(limiter.consume("bucket4j:public:registration:" + IP, 5, Duration.ofMinutes(3)))
                .thenReturn(ConsumptionProbe.rejected(0, 1_200_000_000L, 0));
        when(limiter.consume("bucket4j:public:refresh:" + IP, 5, Duration.ofMinutes(1)))
                .thenReturn(ConsumptionProbe.consumed(4, 0));

        var registration = request("POST", "/profiles");
        var rejected = new MockHttpServletResponse();
        var blockedChain = mock(FilterChain.class);
        filter.doFilter(registration, rejected, blockedChain);
        assertThat(rejected.getStatus()).isEqualTo(429);
        assertThat(rejected.getHeader("Retry-After")).isEqualTo("2");
        assertThat(mapper.readTree(rejected.getContentAsByteArray()).path("code").asInt()).isEqualTo(429);
        verifyNoInteractions(blockedChain);

        var refresh = request("POST", "/auth/refresh");
        var accepted = new MockHttpServletResponse();
        var allowedChain = mock(FilterChain.class);
        filter.doFilter(refresh, accepted, allowedChain);
        verify(allowedChain).doFilter(refresh, accepted);
    }

    @Test
    void loginAndOtherMethodsBypassPublicQuotas() throws Exception {
        for (var request : new MockHttpServletRequest[] {
                request("POST", "/auth/login"), request("GET", "/profiles")
        }) {
            var response = new MockHttpServletResponse();
            var chain = mock(FilterChain.class);
            filter.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);
        }
        verifyNoInteractions(limiter);
    }

    @Test
    void redisFailureReturnsServiceUnavailable() throws Exception {
        when(limiter.consume(anyString(), anyInt(), any()))
                .thenThrow(new DataAccessResourceFailureException("Redis indisponível"));

        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        filter.doFilter(request("POST", "/auth/refresh"), response, chain);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(mapper.readTree(response.getContentAsByteArray()).path("code").asInt()).isEqualTo(503);
        verifyNoInteractions(chain);
    }

    private MockHttpServletRequest request(String method, String path) {
        var request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(IP);
        return request;
    }
}
