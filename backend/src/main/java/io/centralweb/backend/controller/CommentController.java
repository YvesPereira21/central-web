package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.CommentControllerDoc;
import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
import io.centralweb.backend.service.CommentService;
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
public class CommentController implements CommentControllerDoc {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/answer/{answerId}")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable UUID answerId,
            @RequestBody CommentCreateDTO dto,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        CommentDTO comment = commentService.createComment(answerId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @Override
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<Page<CommentDTO>> getCommentsByAnswer(
            @PathVariable UUID answerId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getAllCommentsFromAnswer(answerId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID commentId,
            @RequestBody CommentUpdateDTO dto,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        return ResponseEntity.ok(commentService.updateComment(commentId, dto, userId));
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        commentService.deleteCommentById(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
