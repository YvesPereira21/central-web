package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.AuthenticationControllerDoc;
import io.centralweb.backend.dto.auth.AuthUserDTO;
import io.centralweb.backend.dto.user.LoginRequestDTO;
import io.centralweb.backend.exception.TokenRefreshException;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.security.CookieUtil;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController implements AuthenticationControllerDoc {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                  TokenService tokenService,
                                  RefreshTokenService refreshTokenService,
                                  CookieUtil cookieUtil) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDTO login) {
        var emailPassword = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var auth = this.authenticationManager.authenticate(emailPassword);

        User user = (User) auth.getPrincipal();

        var token = tokenService.generateToken(user);

        refreshTokenService.deleteByUserId(user.getUserId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(token);
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String requestRefreshToken) {
        if (requestRefreshToken == null || requestRefreshToken.isBlank()) {
            throw new TokenRefreshException("", "Refresh token não fornecido nos cookies!");
        }

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenService.generateToken(user);

                    refreshTokenService.deleteByUserId(user.getUserId());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());

                    ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(token);
                    ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(newRefreshToken.getToken());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .<Void>build();
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token não encontrado na base de dados!"));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String requestRefreshToken) {
        if (requestRefreshToken != null && !requestRefreshToken.isBlank()) {
            refreshTokenService.deleteByToken(requestRefreshToken);
        }

        ResponseCookie accessCookie = cookieUtil.deleteAccessTokenCookie();
        ResponseCookie refreshCookie = cookieUtil.deleteRefreshTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<AuthUserDTO> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new AuthUserDTO(user.getUserId(), user.getEmail(), user.getRole()));
    }
}
