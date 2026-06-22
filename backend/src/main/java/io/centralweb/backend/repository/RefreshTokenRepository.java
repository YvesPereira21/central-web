package io.centralweb.backend.repository;

import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByUser(User user);
    
    @Modifying
    void deleteByToken(String token);
}
