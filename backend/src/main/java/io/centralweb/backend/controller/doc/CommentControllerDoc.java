package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
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

@Tag(name = "Comments", description = "Operações relacionadas aos comentários")
public interface CommentControllerDoc {

    @Operation(summary = "Criar comentário", description = "Cria um comentário para uma resposta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Resposta ou Perfil não encontrado")
    })
    ResponseEntity<CommentDTO> createComment(
            UUID answerId,
            @Valid CommentCreateDTO dto,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Listar comentários por resposta", description = "Retorna todos os comentários de uma resposta paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentários retornados com sucesso")
    })
    ResponseEntity<Page<CommentDTO>> getCommentsByAnswer(
            UUID answerId,
            Pageable pageable
    );

    @Operation(summary = "Atualizar comentário", description = "Atualiza o conteúdo de um comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para editar este comentário"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    ResponseEntity<CommentDTO> updateComment(
            UUID commentId,
            @Valid CommentUpdateDTO dto,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Deletar comentário", description = "Deleta um comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comentário deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para deletar este comentário"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    ResponseEntity<Void> deleteComment(
            UUID commentId,
            @Parameter(hidden = true) UUID userId
    );
}
