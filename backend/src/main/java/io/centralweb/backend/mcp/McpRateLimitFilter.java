package io.centralweb.backend.mcp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.centralweb.backend.ratelimit.TokenBucketRateLimiter;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class McpRateLimitFilter extends OncePerRequestFilter {

    private final TokenBucketRateLimiter rateLimiter;

    public McpRateLimitFilter(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
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
        } catch (RuntimeException exception) {
            logger.warn("Não foi possível consultar o limite de requisições MCP no Redis", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (!result.isConsumed()) {
            response.setStatus(429);
            response.setHeader("Retry-After",
                    Long.toString(Math.ceilDiv(result.getNanosToWaitForRefill(), 1_000_000_000L)));
            return;
        }
        chain.doFilter(request, response);
    }
}
