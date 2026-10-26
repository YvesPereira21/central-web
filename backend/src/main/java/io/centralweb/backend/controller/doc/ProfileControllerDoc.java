package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Profile", description = "Gerenciamento de perfis")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public interface ProfileControllerDoc {

    @Operation(summary = "Cria um novo perfil", description = "Cria um perfil a partir dos dados fornecidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    ResponseEntity<ProfileDTO> createProfile(@Valid ProfileCreateDTO profile);

    @Operation(summary = "Busca o próprio perfil", description = "Retorna os detalhes do perfil do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    ResponseEntity<ProfileDTO> getMyProfile(@Parameter(hidden = true) UUID userId);

    @Operation(summary = "Busca perfil por ID", description = "Retorna os detalhes de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    ResponseEntity<ProfileDTO> getProfile(UUID profileId);

    @Operation(summary = "Atualiza um perfil", description = "Atualiza os dados de um perfil existente. Apenas o próprio usuário pode alterar seu perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o dono do perfil)")
    })
    ResponseEntity<ProfileDTO> updateProfile(
            UUID profileId,
            @Valid ProfileUpdateDTO profile,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Exclui um perfil", description = "Remove um perfil existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Perfil removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    ResponseEntity<Void> deleteProfile(
            UUID profileId,
            @Parameter(hidden = true) UUID userProfileId
    );
}
