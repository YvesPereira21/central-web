package io.centralweb.backend.security;

import io.centralweb.backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.centralweb.backend.ratelimit.RateLimitResponses;
import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.BucketExceptions.BucketExecutionException;
import io.github.bucket4j.ConsumptionProbe;
import io.lettuce.core.RedisException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class CreationRateLimitInterceptor implements HandlerInterceptor {
    private record Policy(String action, int capacity, long refillMillis) {}

    private static final Map<String, Policy> POLICIES = Map.of(
            "/profiles", new Policy("profiles", 3, 600_000),
            "/articles", new Policy("articles", 3, 600_000),
            "/questions", new Policy("questions", 5, 120_000),
            "/qualifications", new Policy("qualifications", 5, 600_000),
            "/answers/{questionId}", new Policy("answers", 10, 30_000),
            "/comments/answer/{answerId}", new Policy("comments", 10, 10_000)
    );

    private final TokenBucketRateLimiter rateLimiter;
    private final JsonMapper jsonMapper;

    public CreationRateLimitInterceptor(TokenBucketRateLimiter rateLimiter, JsonMapper jsonMapper) {
        this.rateLimiter = rateLimiter;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!"POST".equals(request.getMethod())) {
            return true;
        }
        Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        Policy policy = route == null ? null : POLICIES.get(route.toString());
        if (policy == null) {
            return true;
        }

        String identity;
        if ("profiles".equals(policy.action())) {
            identity = "ip:" + request.getRemoteAddr();
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User user)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            identity = "user:" + user.getUserId();
        }

        String key = "bucket4j:create:" + policy.action() + ":" + identity;
        ConsumptionProbe result;
        try {
            result = rateLimiter.consume(key, policy.capacity(), Duration.ofMillis(policy.refillMillis()));
        } catch (DataAccessException | RedisException | BucketExecutionException exception) {
            log.warn("Não foi possível consultar a cota de criação para {}", policy.action(), exception);
            RateLimitResponses.write(response, jsonMapper, HttpStatus.SERVICE_UNAVAILABLE);
            return false;
        }
        if (!result.isConsumed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER,
                    Long.toString(Math.ceilDiv(result.getNanosToWaitForRefill(), 1_000_000_000L)));
            RateLimitResponses.write(response, jsonMapper, HttpStatus.TOO_MANY_REQUESTS);
            return false;
        }
        return true;
    }
}
