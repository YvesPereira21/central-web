package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.user.LoginRequestDTO;
import io.centralweb.backend.exception.TokenRefreshException;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.CookieUtil;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private SecurityFilter securityFilter;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    private UUID userId;
    private User user;
    private LoginRequestDTO loginRequestDTO;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User(userId, "password123", "user@test.com");
        user.setRole(UserRole.PERSON);

        loginRequestDTO = new LoginRequestDTO("user@test.com", "password123");

        refreshToken = new RefreshToken();
        refreshToken.setRefreshTokenId(UUID.randomUUID());
        refreshToken.setToken("refresh-token-uuid");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldLoginAndReturn200Ok() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.generateToken(user)).thenReturn("access-token-jwt");
        doNothing().when(refreshTokenService).deleteByUserId(userId);
        when(refreshTokenService.createRefreshToken(userId)).thenReturn(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, "access-token-jwt").build();
        ResponseCookie refreshCookie = ResponseCookie.from(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "refresh-token-uuid").build();
        when(cookieUtil.createAccessTokenCookie("access-token-jwt")).thenReturn(accessCookie);
        when(cookieUtil.createRefreshTokenCookie("refresh-token-uuid")).thenReturn(refreshCookie);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void shouldRefreshAndReturn200Ok() throws Exception {
        String refreshCookieValue = "valid-refresh-token";
        when(refreshTokenService.findByToken(refreshCookieValue)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(tokenService.generateToken(user)).thenReturn("new-access-token");
        doNothing().when(refreshTokenService).deleteByUserId(userId);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");
        when(refreshTokenService.createRefreshToken(userId)).thenReturn(newRefreshToken);

        ResponseCookie accessCookie = ResponseCookie.from(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, "new-access-token").build();
        ResponseCookie refreshCookie = ResponseCookie.from(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "new-refresh-token").build();
        when(cookieUtil.createAccessTokenCookie("new-access-token")).thenReturn(accessCookie);
        when(cookieUtil.createRefreshTokenCookie("new-refresh-token")).thenReturn(refreshCookie);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshCookieValue)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void shouldLogoutAndReturn204NoContent() throws Exception {
        String refreshCookieValue = "valid-refresh-token";
        doNothing().when(refreshTokenService).deleteByToken(refreshCookieValue);

        ResponseCookie deleteAccessCookie = ResponseCookie.from(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, "").maxAge(0).build();
        ResponseCookie deleteRefreshCookie = ResponseCookie.from(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "").maxAge(0).build();
        when(cookieUtil.deleteAccessTokenCookie()).thenReturn(deleteAccessCookie);
        when(cookieUtil.deleteRefreshTokenCookie()).thenReturn(deleteRefreshCookie);

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshCookieValue)))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void shouldLogoutAndReturn204NoContentWhenCookieIsMissing() throws Exception {
        ResponseCookie deleteAccessCookie = ResponseCookie.from(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, "").maxAge(0).build();
        ResponseCookie deleteRefreshCookie = ResponseCookie.from(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "").maxAge(0).build();
        when(cookieUtil.deleteAccessTokenCookie()).thenReturn(deleteAccessCookie);
        when(cookieUtil.deleteRefreshTokenCookie()).thenReturn(deleteRefreshCookie);

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotLoginAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        LoginRequestDTO invalidLogin = new LoginRequestDTO("invalid-email", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotLoginAndReturn500InternalServerErrorWhenCredentialsAreInvalid() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldNotRefreshAndReturn403ForbiddenWhenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotRefreshAndReturn403ForbiddenWhenTokenIsInvalidOrExpired() throws Exception {
        String refreshCookieValue = "invalid-or-expired-token";
        when(refreshTokenService.findByToken(refreshCookieValue)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshCookieValue)))
                .andExpect(status().isForbidden());
    }
}
