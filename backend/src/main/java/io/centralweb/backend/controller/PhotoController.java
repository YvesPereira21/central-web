package io.centralweb.backend.controller;

import io.centralweb.backend.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.centralweb.backend.security.SecurityConfigurations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/photos")
@Tag(name = "Photo", description = "Gerenciamento de fotos de perfil")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(value = "/{profileId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz o upload da foto do usuário", description = "Faz o upload de um arquivo de imagem e salva no perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao salvar a imagem")
    })
    public ResponseEntity<Void> uploadAvatar(
            @PathVariable UUID profileId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            photoService.uploadAvatar(profileId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
