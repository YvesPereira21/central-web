package io.centralweb.backend.controller;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.centralweb.backend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping(path = "/tags")
@Validated
@Tag(name = "Tag", description = "Gerenciamento de tags")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("")
    @Operation(summary = "Cria uma nova tag", description = "Cria uma tag a partir dos dados fornecidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tag criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tag já existente")
    })
    public ResponseEntity<TagDTO> createTag(@RequestBody @Valid TagDTO tag) {
        TagDTO newTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }

    @GetMapping("")
    @Operation(summary = "Lista todas as tags", description = "Retorna a lista completa de tags cadastradas no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tags retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma tag encontrada")
    })
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.status(HttpStatus.OK).body(tags);
    }

    @PutMapping("/{tagId}")
    @Operation(summary = "Atualiza uma tag existente", description = "Atualiza os dados de uma tag identificada pelo UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados mal formatados)"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada para o ID informado")
    })
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable UUID tagId,
            @RequestBody @Valid TagUpdateDTO tagUpdated
    ) {
        TagDTO tag = tagService.updateTag(tagId, tagUpdated);
        return ResponseEntity.status(HttpStatus.OK).body(tag);
    }

    @DeleteMapping("/{technologyName}")
    @Operation(summary = "Remove uma tag pelo nome", description = "Exclui a tag correspondente ao nome de tecnologia informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag removida com sucesso (sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada para o nome de tecnologia fornecido")
    })
    public ResponseEntity<Void> deleteTag(
            @PathVariable String technologyName
    ) {
        tagService.deleteTagByTechnologyName(technologyName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}