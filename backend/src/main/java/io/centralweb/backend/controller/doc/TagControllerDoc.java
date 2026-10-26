package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Tag", description = "Gerenciamento de tags")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public interface TagControllerDoc {

    @Operation(summary = "Cria uma nova tag", description = "Cria uma tag a partir dos dados fornecidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tag criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tag já existente")
    })
    ResponseEntity<TagDTO> createTag(@Valid TagDTO tag);

    @Operation(summary = "Lista todas as tags", description = "Retorna a lista completa de tags cadastradas no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tags retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma tag encontrada")
    })
    ResponseEntity<Page<TagDTO>> getAllTags(Pageable pageable);

    @Operation(summary = "Atualiza uma tag existente", description = "Atualiza os dados de uma tag identificada pelo UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados mal formatados)"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada para o ID informado")
    })
    ResponseEntity<TagDTO> updateTag(
            UUID tagId,
            @Valid TagUpdateDTO tagUpdated
    );

    @Operation(summary = "Remove uma tag pelo nome", description = "Exclui a tag correspondente ao nome de tecnologia informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag removida com sucesso (sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada para o nome de tecnologia fornecido")
    })
    ResponseEntity<Void> deleteTag(String technologyName);
}
