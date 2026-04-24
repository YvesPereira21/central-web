package io.centralweb.backend.controller;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.centralweb.backend.service.ProfileService;
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

import java.util.UUID;
@RestController
@RequestMapping(path = "/profiles")
@Validated
@Tag(name = "Profile", description = "Gerenciamento de perfis")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("")
    @Operation(summary = "Cria um novo perfil", description = "Cria um perfil a partir dos dados fornecidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<ProfileDTO> createProfile(@RequestBody @Valid ProfileCreateDTO profile) {
        ProfileDTO profileUniqueDTO = profileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileUniqueDTO);
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Busca perfil por ID", description = "Retorna os detalhes de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable UUID profileId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId));
    }

    @PutMapping("/{profileId}")
    @Operation(summary = "Atualiza um perfil", description = "Atualiza os dados de um perfil existente. Apenas o próprio usuário pode alterar seu perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o dono do perfil)")
    })
    public ResponseEntity<ProfileDTO> updateProfile(
            @PathVariable UUID profileId,
            @RequestBody @Valid ProfileUpdateDTO profile,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(profileService.updateProfile(profileId, profile, userId));
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "Exclui um perfil", description = "Remove um perfil existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Perfil removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID profileId) {
        profileService.deleteProfileById(profileId);
        return ResponseEntity.noContent().build();
    }
}
