package io.centralweb.backend.ratelimit;

import io.centralweb.backend.dto.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public final class RateLimitResponses {
    private RateLimitResponses() {}

    public static ApiError body(HttpStatus status) {
        String message = status == HttpStatus.TOO_MANY_REQUESTS
                ? "Limite de requisições excedido. Tente novamente mais tarde."
                : "Serviço temporariamente indisponível.";
        return new ApiError(LocalDateTime.now(), status.value(), status.name(), List.of(message));
    }

    public static void write(HttpServletResponse response, JsonMapper mapper, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), body(status));
    }
}
