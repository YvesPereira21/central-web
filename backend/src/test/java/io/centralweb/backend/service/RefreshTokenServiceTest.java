package io.centralweb.backend.service;

import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.TokenRefreshException;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.RefreshTokenRepository;
import io.centralweb.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        ReflectionTestUtils.setField(user, "userId", userId);
        user.setEmail("user@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        // Inject the value of refreshTokenDurationMs manually as Mockito won't process @Value
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 3600000L); // 1 hour in ms
    }

    @Test
    void createRefreshToken_ShouldReturnRefreshToken_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken(userId);

        assertNotNull(created);
        assertEquals(user, created.getUser());
        assertNotNull(created.getToken());
        assertTrue(created.getExpiryDate().isAfter(Instant.now()));
        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ShouldThrowObjectNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> refreshTokenService.createRefreshToken(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_ShouldReturnToken_WhenFound() {
        String tokenStr = refreshToken.getToken();
        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> found = refreshTokenService.findByToken(tokenStr);

        assertTrue(found.isPresent());
        assertEquals(refreshToken, found.get());
        verify(refreshTokenRepository, times(1)).findByToken(tokenStr);
    }

    @Test
    void deleteByUserId_ShouldDelete_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> refreshTokenService.deleteByUserId(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    @Test
    void deleteByUserId_ShouldThrowObjectNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> refreshTokenService.deleteByUserId(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
    }

    @Test
    void deleteByToken_ShouldDelete() {
        String tokenStr = refreshToken.getToken();
        assertDoesNotThrow(() -> refreshTokenService.deleteByToken(tokenStr));
        verify(refreshTokenRepository, times(1)).deleteByToken(tokenStr);
    }

    @Test
    void verifyExpiration_ShouldReturnToken_WhenNotExpired() {
        RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);
        assertEquals(refreshToken, result);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ShouldThrowTokenRefreshException_WhenExpired() {
        refreshToken.setExpiryDate(Instant.now().minusSeconds(10));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyExpiration(refreshToken));
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }
}
