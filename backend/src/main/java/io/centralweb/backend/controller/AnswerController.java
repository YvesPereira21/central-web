package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.AnswerControllerDoc;
import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.service.AnswerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/answers")
@Validated
public class AnswerController implements AnswerControllerDoc {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/{questionId}")
    public ResponseEntity<AnswerDTO> createAnswer(
            @RequestBody AnswerCreateDTO answer,
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        AnswerDTO newAwnser = answerService.createAnswer(questionId, answer, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAwnser);
    }

    @Override
    @GetMapping("/{questionId}")
    public ResponseEntity<Page<AnswerDTO>> getAllAnswersFromQuestion(
            @PathVariable UUID questionId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(answerService.getAllAnswersFromQuestion(questionId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{answerId}")
    public ResponseEntity<Void> acceptAnswer(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        answerService.acceptAnswer(answerId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{answerId}/like")
    public ResponseEntity<Void> toggleAnswerLike(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userId
    ) {
        answerService.toggleAnswerLike(answerId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        answerService.deleteAnswerById(answerId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
