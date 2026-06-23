package io.centralweb.backend.service;

import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.TokenRefreshException;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.RefreshTokenRepository;
import io.centralweb.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {
    @Value("${api.security.refresh.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(UUID userId) {
        log.info("Criando refresh token para o ID de usuário: '{}'", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário inexistente"));

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token criado com sucesso para o ID de usuário: '{}'", userId);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.debug("Buscando refresh token");
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        log.info("Excluindo refresh token para o ID de usuário: '{}'", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário inexistente"));

        refreshTokenRepository.deleteByUser(user);
        log.info("Refresh token excluído com sucesso para o ID de usuário: '{}'", userId);
    }

    @Transactional
    public void deleteByToken(String token) {
        log.debug("Excluindo refresh token por string de token");
        refreshTokenRepository.deleteByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        log.debug("Verificando expiração do refresh token");
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            log.warn("Refresh token expirado para o ID de usuário: '{}'", token.getUser().getUserId());
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    token.getToken(), "Refresh token foi expirado. Por favor, faça login novamente"
            );
        }
        return token;
    }
}
