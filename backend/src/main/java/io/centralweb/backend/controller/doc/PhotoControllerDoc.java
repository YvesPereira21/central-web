package io.centralweb.backend.controller.doc;

import io.centralweb.backend.security.SecurityConfigurations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Photo", description = "Gerenciamento de fotos de perfil")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public interface PhotoControllerDoc {

    @Operation(summary = "Faz o upload da foto do usuário", description = "Faz o upload de um arquivo de imagem e salva no perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao salvar a imagem")
    })
    ResponseEntity<Void> uploadAvatar(
            UUID profileId,
            MultipartFile file
    );
}
