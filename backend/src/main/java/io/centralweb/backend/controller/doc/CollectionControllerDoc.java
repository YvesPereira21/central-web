package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.dto.collection.CollectionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Collections", description = "Operações relacionadas às coleções/favoritos de artigos e perguntas")
public interface CollectionControllerDoc {

    @Operation(summary = "Criar coleção", description = "Cria uma nova pasta de coleção para o usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coleção criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<CollectionDTO> createCollection(
            @Valid CollectionCreateDTO dto,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Listar minhas coleções", description = "Retorna todas as coleções do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coleções retornadas com sucesso")
    })
    ResponseEntity<Page<CollectionDTO>> getMyCollections(
            @Parameter(hidden = true) UUID userId,
            Pageable pageable
    );

    @Operation(summary = "Buscar coleção", description = "Retorna os detalhes de uma coleção do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coleção retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Coleção não encontrada")
    })
    ResponseEntity<CollectionDTO> getCollectionById(
            UUID collectionId,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Adicionar artigo", description = "Adiciona um artigo a uma coleção")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artigo adicionado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Coleção ou artigo não encontrado")
    })
    ResponseEntity<Void> addArticleToCollection(
            UUID collectionId,
            UUID articleId,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Adicionar pergunta", description = "Adiciona uma pergunta a uma coleção")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pergunta adicionada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Coleção ou pergunta não encontrada")
    })
    ResponseEntity<Void> addQuestionToCollection(
            UUID collectionId,
            UUID questionId,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Remover artigo de todas as coleções", description = "Remove o artigo de todas as coleções do usuário atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artigo removido com sucesso")
    })
    ResponseEntity<Void> removeArticleFromAllMyCollections(
            UUID articleId,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Remover pergunta de todas as coleções", description = "Remove a pergunta de todas as coleções do usuário atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pergunta removida com sucesso")
    })
    ResponseEntity<Void> removeQuestionFromAllMyCollections(
            UUID questionId,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Deletar comentário", description = "Deleta uma pasta de favoritos/coleções")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Coleção deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para deletar esta coleção"),
            @ApiResponse(responseCode = "404", description = "Coleção não encontrada")
    })
    ResponseEntity<Void> deleteCollection(
            UUID collectionId,
            @Parameter(hidden = true) UUID userId
    );
}
