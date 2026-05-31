package io.centralweb.backend.controller;

import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.dto.collection.CollectionDTO;
import io.centralweb.backend.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/collections")
@Tag(name = "Collections", description = "Operações relacionadas às coleções/favoritos de artigos e perguntas")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping
    @Operation(summary = "Criar coleção", description = "Cria uma nova pasta de coleção para o usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coleção criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<CollectionDTO> createCollection(
            @RequestBody @Valid CollectionCreateDTO dto,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.createCollection(dto, userId));
    }

    @PreAuthorize("hasRole('PERSON')")
    @GetMapping("/my-collections")
    @Operation(summary = "Listar minhas coleções", description = "Retorna todas as coleções do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coleções retornadas com sucesso")
    })
    public ResponseEntity<Page<CollectionDTO>> getMyCollections(
            @AuthenticationPrincipal(expression = "userId") UUID userId,
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(collectionService.getMyCollections(userId, pageable));
    }

    @PreAuthorize("hasRole('PERSON')")
    @GetMapping("/{collectionId}")
    @Operation(summary = "Buscar coleção", description = "Retorna os detalhes de uma coleção do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coleção retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Coleção não encontrada")
    })
    public ResponseEntity<CollectionDTO> getCollectionById(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(collectionService.getCollectionById(collectionId, userId));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/{collectionId}/articles/{articleId}")
    @Operation(summary = "Adicionar artigo", description = "Adiciona um artigo a uma coleção")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artigo adicionado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Coleção ou artigo não encontrado")
    })
    public ResponseEntity<Void> addArticleToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.addArticleToCollection(collectionId, articleId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/{collectionId}/questions/{questionId}")
    @Operation(summary = "Adicionar pergunta", description = "Adiciona uma pergunta a uma coleção")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pergunta adicionada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Coleção ou pergunta não encontrada")
    })
    public ResponseEntity<Void> addQuestionToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.addQuestionToCollection(collectionId, questionId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PERSON')")
    @DeleteMapping("/articles/{articleId}")
    @Operation(summary = "Remover artigo de todas as coleções", description = "Remove o artigo de todas as coleções do usuário atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artigo removido com sucesso")
    })
    public ResponseEntity<Void> removeArticleFromAllMyCollections(
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.removeArticleFromAllMyCollections(articleId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PERSON')")
    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "Remover pergunta de todas as coleções", description = "Remove a pergunta de todas as coleções do usuário atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pergunta removida com sucesso")
    })
    public ResponseEntity<Void> removeQuestionFromAllMyCollections(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.removeQuestionFromAllMyCollections(questionId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{collectionId}")
    @Operation(summary = "Deletar comentário", description = "Deleta uma pasta de favoritos/coleções")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Coleção deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para deletar esta coleção"),
            @ApiResponse(responseCode = "404", description = "Coleção não encontrada")
    })
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        collectionService.deleteCollectionById(collectionId, userId);
        return ResponseEntity.noContent().build();
    }
}
