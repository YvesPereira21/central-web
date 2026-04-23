package io.centralweb.backend.controller;

import io.centralweb.backend.dto.answer.AnswerAcceptedDTO;
import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.model.User;
import io.centralweb.backend.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/answers")
@Validated
public class AnswerController {
    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("")
    public ResponseEntity<AnswerDTO> createAnswer(
            @RequestBody @Valid AnswerCreateDTO answer,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        AnswerDTO newAwnser = answerService.createAnswer(answer, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAwnser);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<List<AnswerDTO>> getAllAnswersFromQuestion(@PathVariable UUID questionId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(answerService.getAllAnswersFromQuestion(questionId));
    }

    @PatchMapping("")
    public ResponseEntity<Void> acceptAnswer(
            AnswerAcceptedDTO answerAccepted,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        answerService.acceptAnswer(answerAccepted, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable UUID answerId) {
        answerService.deleteAnswerById(answerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
