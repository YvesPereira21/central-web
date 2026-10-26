package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.auth.AuthUserDTO;
import io.centralweb.backend.dto.user.LoginRequestDTO;
import io.centralweb.backend.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "Autenticação de usuários")
public interface AuthenticationControllerDoc {

    @Operation(summary = "Realiza login", description = "Autentica um usuário e define cookies HttpOnly de acesso e refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, cookies definidos"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados de login mal formatados)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    ResponseEntity<Void> login(@Valid LoginRequestDTO login);

    @Operation(summary = "Atualiza o Access Token", description = "Gera um novo access token a partir do cookie HttpOnly de refresh token")
    ResponseEntity<Void> refresh(String requestRefreshToken);

    @Operation(summary = "Realiza logout", description = "Invalida o refresh token no banco e limpa os cookies HttpOnly")
    ResponseEntity<Void> logout(String requestRefreshToken);

    @Operation(summary = "Obtém usuário autenticado", description = "Retorna os dados básicos do usuário logado (userId, email, role)")
    ResponseEntity<AuthUserDTO> me(@Parameter(hidden = true) User user);
}
