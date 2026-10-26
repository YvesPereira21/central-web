package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Qualification", description = "Gerenciamento de qualificações")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public interface QualificationControllerDoc {

    @Operation(summary = "Cria uma qualificação", description = "Cria uma nova qualificação associada ao usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Qualificação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    ResponseEntity<QualificationDTO> createQualification(
            @Valid QualificationCreateDTO qualification,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Lista qualificações verificadas", description = "Retorna todas as qualificações que foram verificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações verificadas")
    })
    ResponseEntity<Page<QualificationDTO>> getVerifiedQualifications(Pageable pageable);

    @Operation(summary = "Lista qualificações não verificadas", description = "Retorna todas as qualificações que ainda não foram verificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações não verificadas")
    })
    ResponseEntity<Page<QualificationDTO>> getNotVerifiedQualifications(Pageable pageable);

    @Operation(summary = "Qualificações verificadas de um perfil", description = "Retorna as qualificações verificadas de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações verificadas do perfil"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    ResponseEntity<Page<QualificationDTO>> getProfileVerifiedQualifications(
            UUID profileId,
            Pageable pageable
    );

    @Operation(summary = "Qualificações não verificadas de um perfil", description = "Retorna as qualificações não verificadas de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações não verificadas do perfil"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    ResponseEntity<Page<QualificationDTO>> getProfileNotVerifiedQualifications(
            UUID profileId,
            Pageable pageable
    );

    @Operation(summary = "Verifica uma qualificação", description = "Marca uma qualificação como verificada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Qualificação marcada como verificada com sucesso")
    })
    ResponseEntity<Void> verify(UUID qualificationId);

    @Operation(summary = "Exclui uma qualificação", description = "Remove uma qualificação existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Qualificação removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Qualificação não encontrada")
    })
    ResponseEntity<Void> deleteQualification(
            UUID qualificationId,
            @Parameter(hidden = true) UUID userProfileId
    );
}
