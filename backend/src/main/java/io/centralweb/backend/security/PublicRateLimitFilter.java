package io.centralweb.backend.security;

import io.centralweb.backend.ratelimit.RateLimitResponses;
import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.BucketExceptions.BucketExecutionException;
import io.github.bucket4j.ConsumptionProbe;
import io.lettuce.core.RedisException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class PublicRateLimitFilter extends OncePerRequestFilter {

    private record Policy(PathPatternRequestMatcher matcher, String name, int capacity, Duration refillPeriod) {}

    private static final PathPatternRequestMatcher.Builder PATHS = PathPatternRequestMatcher.withDefaults();
    private static final List<Policy> POLICIES = List.of(
            new Policy(PATHS.matcher(HttpMethod.POST, "/auth/refresh"), "refresh", 5, Duration.ofMinutes(1)),
            new Policy(PATHS.matcher(HttpMethod.POST, "/profiles"), "registration", 5, Duration.ofMinutes(3))
    );

    private final TokenBucketRateLimiter rateLimiter;
    private final JsonMapper jsonMapper;

    public PublicRateLimitFilter(TokenBucketRateLimiter rateLimiter, JsonMapper jsonMapper) {
        this.rateLimiter = rateLimiter;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Policy policy = policyFor(request);
        if (policy == null) {
            chain.doFilter(request, response);
            return;
        }

        String key = "bucket4j:public:" + policy.name() + ":" + request.getRemoteAddr();
        ConsumptionProbe result;
        try {
            result = rateLimiter.consume(key, policy.capacity(), policy.refillPeriod());
        } catch (DataAccessException | RedisException | BucketExecutionException exception) {
            logger.warn("Não foi possível consultar a cota de " + policy.name() + " no Redis", exception);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            RateLimitResponses.write(response, jsonMapper, HttpStatus.SERVICE_UNAVAILABLE);
            return;
        }

        if (result.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }

        long seconds = Math.max(1, Math.ceilDiv(result.getNanosToWaitForRefill(), 1_000_000_000L));
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(seconds));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        RateLimitResponses.write(response, jsonMapper, HttpStatus.TOO_MANY_REQUESTS);
    }

    private Policy policyFor(HttpServletRequest request) {
        for (Policy policy : POLICIES) {
            if (policy.matcher().matches(request)) {
                return policy;
            }
        }
        return null;
    }
}
