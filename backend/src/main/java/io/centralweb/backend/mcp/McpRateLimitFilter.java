package io.centralweb.backend.mcp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.centralweb.backend.ratelimit.RateLimitResponses;
import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.BucketExceptions.BucketExecutionException;
import io.github.bucket4j.ConsumptionProbe;
import io.lettuce.core.RedisException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Component
public class McpRateLimitFilter extends OncePerRequestFilter {

    private final TokenBucketRateLimiter rateLimiter;
    private final JsonMapper jsonMapper;

    public McpRateLimitFilter(TokenBucketRateLimiter rateLimiter, JsonMapper jsonMapper) {
        this.rateLimiter = rateLimiter;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod())
                || !request.getRequestURI().equals(request.getContextPath() + "/mcp");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = "bucket4j:mcp:" + request.getRemoteAddr();
        ConsumptionProbe result;
        try {
            result = rateLimiter.consume(key, 10, Duration.ofSeconds(1));
        } catch (DataAccessException | RedisException | BucketExecutionException exception) {
            logger.warn("Não foi possível consultar o limite de requisições MCP no Redis", exception);
            RateLimitResponses.write(response, jsonMapper, HttpStatus.SERVICE_UNAVAILABLE);
            return;
        }
        if (!result.isConsumed()) {
            response.setHeader("Retry-After",
                    Long.toString(Math.ceilDiv(result.getNanosToWaitForRefill(), 1_000_000_000L)));
            RateLimitResponses.write(response, jsonMapper, HttpStatus.TOO_MANY_REQUESTS);
            return;
        }
        chain.doFilter(request, response);
    }
}
