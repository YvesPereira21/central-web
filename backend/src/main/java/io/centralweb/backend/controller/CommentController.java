package io.centralweb.backend.controller;

import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
import io.centralweb.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@Tag(name = "Comments", description = "Operações relacionadas aos comentários")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/answer/{answerId}")
    @Operation(summary = "Criar comentário", description = "Cria um comentário para uma resposta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Resposta ou Perfil não encontrado")
    })
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable UUID answerId,
            @RequestBody @Valid CommentCreateDTO dto,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        CommentDTO comment = commentService.createComment(answerId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/answer/{answerId}")
    @Operation(summary = "Listar comentários por resposta", description = "Retorna todos os comentários de uma resposta paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentários retornados com sucesso")
    })
    public ResponseEntity<Page<CommentDTO>> getCommentsByAnswer(
            @PathVariable UUID answerId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getAllCommentsFromAnswer(answerId, pageable));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{commentId}")
    @Operation(summary = "Atualizar comentário", description = "Atualiza o conteúdo de um comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para editar este comentário"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentUpdateDTO dto,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        return ResponseEntity.ok(commentService.updateComment(commentId, dto, userId));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Deletar comentário", description = "Deleta um comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comentário deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para deletar este comentário"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        commentService.deleteCommentById(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
