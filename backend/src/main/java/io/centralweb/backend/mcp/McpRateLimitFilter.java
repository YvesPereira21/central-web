package io.centralweb.backend.mcp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class McpRateLimitFilter extends OncePerRequestFilter {

    private static final DefaultRedisScript<Long> CONSUME_TOKEN = new DefaultRedisScript<>("""
            -- Um token equivale a 1000 créditos; cada milissegundo repõe um crédito.
            local capacity = 10000
            local cost = 1000
            local clock = redis.call('TIME')
            local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
            local state = redis.call('HMGET', KEYS[1], 'credits', 'updatedAt')
            local credits = tonumber(state[1]) or capacity
            local updatedAt = tonumber(state[2]) or now
            local effectiveNow = math.max(now, updatedAt)
            credits = math.min(capacity, credits + effectiveNow - updatedAt)

            local waitMs = 0
            if credits >= cost then
                credits = credits - cost
            else
                waitMs = cost - credits
            end

            redis.call('HSET', KEYS[1], 'credits', credits, 'updatedAt', effectiveNow)
            redis.call('PEXPIRE', KEYS[1], capacity)
            return waitMs
            """, Long.class);

    private final StringRedisTemplate redis;

    public McpRateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod())
                || !request.getRequestURI().equals(request.getContextPath() + "/mcp");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = "rate-limit:mcp:" + request.getRemoteAddr();
        Long waitMs;
        try {
            waitMs = redis.execute(CONSUME_TOKEN, List.of(key));
        } catch (DataAccessException exception) {
            logger.warn("Não foi possível consultar o limite de requisições MCP no Redis", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        if (waitMs == null) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (waitMs > 0) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString((waitMs + 999) / 1000));
            return;
        }
        chain.doFilter(request, response);
    }
}
