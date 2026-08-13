package io.centralweb.backend.controller;

import io.centralweb.backend.dto.user.*;
import io.centralweb.backend.exception.TokenRefreshException;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.security.CookieUtil;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Autenticação de usuários")
public class AuthenticationController {

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

    @PostMapping("/login")
    @Operation(summary = "Realiza login", description = "Autentica um usuário e define cookies HttpOnly de acesso e refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, cookies definidos"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados de login mal formatados)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequestDTO login) {
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

    @PostMapping("/refresh")
    @Operation(summary = "Atualiza o Access Token", description = "Gera um novo access token a partir do cookie HttpOnly de refresh token")
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

    @PostMapping("/logout")
    @Operation(summary = "Realiza logout", description = "Invalida o refresh token no banco e limpa os cookies HttpOnly")
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

    @GetMapping("/me")
    @Operation(summary = "Obtém usuário autenticado", description = "Retorna os dados básicos do usuário logado (userId, email, role)")
    public ResponseEntity<io.centralweb.backend.dto.auth.AuthUserDTO> me(@org.springframework.security.core.annotation.AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new io.centralweb.backend.dto.auth.AuthUserDTO(user.getUserId(), user.getEmail(), user.getRole()));
    }
}
