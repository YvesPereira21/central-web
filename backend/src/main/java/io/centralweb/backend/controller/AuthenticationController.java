package io.centralweb.backend.controller;

import io.centralweb.backend.dto.user.LoginRequestDTO;
import io.centralweb.backend.dto.user.LoginResponseDTO;
import io.centralweb.backend.model.User;
import io.centralweb.backend.security.TokenService;
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

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza login", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, token gerado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados de login mal formatados)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO login){
        var emailPassword = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var auth = this.authenticationManager.authenticate(emailPassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
