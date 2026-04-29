package io.centralweb.backend.controller;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.model.User;
import io.centralweb.backend.security.SecurityConfigurations;
import io.centralweb.backend.service.QualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/qualifications")
@Validated
@Tag(name = "Qualification", description = "Gerenciamento de qualificações")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class QualificationController {

    private final QualificationService qualificationService;

    public QualificationController(QualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    @PostMapping("")
    @Operation(summary = "Cria uma qualificação", description = "Cria uma nova qualificação associada ao usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Qualificação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<QualificationDTO> createQualification(
            @RequestBody @Valid
            QualificationCreateDTO qualification,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        QualificationDTO newQualification = qualificationService
                .createQualification(qualification, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newQualification);
    }

    @GetMapping("/verified")
    @Operation(summary = "Lista qualificações verificadas", description = "Retorna todas as qualificações que foram verificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações verificadas")
    })
    public ResponseEntity<List<QualificationDTO>> getVerifiedQualifications() {
        return ResponseEntity.ok(qualificationService.getAllQualificationsVerified());
    }

    @GetMapping("/not-verified")
    @Operation(summary = "Lista qualificações não verificadas", description = "Retorna todas as qualificações que ainda não foram verificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações não verificadas")
    })
    public ResponseEntity<List<QualificationDTO>> getNotVerifiedQualifications() {
        return ResponseEntity.ok(qualificationService.getAllNotVerifiedQualifications());
    }

    @GetMapping("/{profileId}/verified")
    @Operation(summary = "Qualificações verificadas de um perfil", description = "Retorna as qualificações verificadas de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações verificadas do perfil"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<List<QualificationDTO>> getProfileVerifiedQualifications(
            @PathVariable UUID profileId
    ) {
        return ResponseEntity.ok(qualificationService.getAllProfileVerifiedQualifications(profileId));
    }

    @GetMapping("/{profileId}/not-verified")
    @Operation(summary = "Qualificações não verificadas de um perfil", description = "Retorna as qualificações não verificadas de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de qualificações não verificadas do perfil"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<List<QualificationDTO>> getProfileNotVerifiedQualifications(
            @PathVariable UUID profileId
    ) {
        return ResponseEntity.ok(qualificationService.getAllProfileNotVerifiedQualifications(profileId));
    }

    @DeleteMapping("/{qualificationId}")
    @Operation(summary = "Exclui uma qualificação", description = "Remove uma qualificação existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Qualificação removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Qualificação não encontrada")
    })
    public ResponseEntity<Void> deleteQualification(@PathVariable UUID qualificationId) {
        qualificationService.deleteQualificationById(qualificationId);
        return ResponseEntity.noContent().build();
    }
}
