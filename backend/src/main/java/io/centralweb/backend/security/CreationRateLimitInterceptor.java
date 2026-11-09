package io.centralweb.backend.security;

import io.centralweb.backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;

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

    private static final DefaultRedisScript<Long> CONSUME_TOKEN = new DefaultRedisScript<>("""
            local tokenCost = tonumber(ARGV[2])
            local maxCredits = tonumber(ARGV[1]) * tokenCost
            local clock = redis.call('TIME')
            local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
            local state = redis.call('HMGET', KEYS[1], 'credits', 'updatedAt')
            local credits = tonumber(state[1]) or maxCredits
            local updatedAt = tonumber(state[2]) or now
            local effectiveNow = math.max(now, updatedAt)
            credits = math.min(maxCredits, credits + effectiveNow - updatedAt)
            local waitMillis = 0
            if credits >= tokenCost then
                credits = credits - tokenCost
            else
                waitMillis = tokenCost - credits
            end
            redis.call('HSET', KEYS[1], 'credits', credits, 'updatedAt', effectiveNow)
            redis.call('PEXPIRE', KEYS[1], maxCredits)
            return waitMillis
            """, Long.class);

    private final StringRedisTemplate redis;

    public CreationRateLimitInterceptor(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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

        String key = "rate-limit:create:" + policy.action() + ":" + identity;
        Long waitMillis;
        try {
            waitMillis = redis.execute(CONSUME_TOKEN, List.of(key),
                    Integer.toString(policy.capacity()), Long.toString(policy.refillMillis()));
        } catch (DataAccessException exception) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
        if (waitMillis == null) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
        if (waitMillis > 0) {
            response.setStatus(429);
            response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString((waitMillis + 999) / 1000));
            return false;
        }
        return true;
    }
}
