package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.QuestionControllerDoc;
import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.service.QuestionService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/questions")
@Validated
public class QuestionController implements QuestionControllerDoc {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("")
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestBody QuestionCreateDTO question,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        QuestionDTO newQuestion = questionService.createQuestion(question, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newQuestion);
    }

    @Override
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> getQuestion(
            @PathVariable("questionId") UUID questionId
    ) {
        QuestionDTO question = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(question);
    }

    @Override
    @GetMapping("")
    public ResponseEntity<Page<QuestionListDTO>> getAllPublishedQuestions(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestions(pageable));
    }

    @Override
    @GetMapping("/{profileId}/profile")
    public ResponseEntity<Page<QuestionListDTO>> getProfileQuestions(
            @PathVariable UUID profileId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(questionService.getAllQuestionsByProfile(profileId, userId, pageable));
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<QuestionListDTO>> searchPublishedQuestions(
            @RequestParam("query") String query,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.searchPublishedQuestions(query, pageable));
    }

    @Override
    @GetMapping("/{technologyName}/tag")
    public ResponseEntity<Page<QuestionListDTO>> getQuestionsByTag(
            @PathVariable String technologyName,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestionsByTechnologyName(
                technologyName, pageable
        ));
    }

    @Override
    @GetMapping("/filter")
    public ResponseEntity<Page<QuestionListDTO>> getQuestionsByTags(
            @RequestParam List<String> tags,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestionsByTags(
                tags, pageable
        ));
    }

    @Override
    @GetMapping("/accepteds-answers")
    public ResponseEntity<Page<QuestionListDTO>> getQuestionsWithAcceptedAnswer(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestionWithAcceptedAnswer(
                pageable
        ));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable UUID questionId,
            @RequestBody QuestionUpdateDTO question,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.updateQuestion(questionId, question, userId));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{questionId}/like")
    public ResponseEntity<Void> toggleQuestionLike(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userId
    ) {
        questionService.toggleQuestionLike(questionId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        questionService.deleteQuestionById(questionId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
