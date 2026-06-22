package io.centralweb.backend.controller;

import io.centralweb.backend.dto.user.*;
import io.centralweb.backend.exception.TokenRefreshException;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Autenticação de usuários")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza login", description = "Autentica um usuário e retorna tokens de acesso e refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, tokens gerados"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados de login mal formatados)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO login){
        var emailPassword = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var auth = this.authenticationManager.authenticate(emailPassword);
        
        User user = (User) auth.getPrincipal();

        var token = tokenService.generateToken(user);
        
        // Deleta tokens antigos do usuário e cria um novo refresh token
        refreshTokenService.deleteByUserId(user.getUserId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        return ResponseEntity.ok(new LoginResponseDTO(token, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualiza o Access Token", description = "Gera um novo access token a partir de um refresh token válido")
    public ResponseEntity<TokenRefreshResponseDTO> refresh(@RequestBody @Valid TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenService.generateToken(user);
                    
                    // Rotaciona o refresh token
                    refreshTokenService.deleteByUserId(user.getUserId());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());
                    
                    return ResponseEntity.ok(new TokenRefreshResponseDTO(token, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token não encontrado na base de dados!"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Realiza logout", description = "Invalida o refresh token no banco de dados")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequestDTO request) {
        refreshTokenService.deleteByToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
