package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_token", columnList = "token", unique = true)
        }
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "refresh_token_id")
    private UUID refreshTokenId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public RefreshToken() {}

    public RefreshToken(String token, Instant expiryDate, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }

    public UUID getRefreshTokenId() {
        return refreshTokenId;
    }

    public void setRefreshTokenId(UUID refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
