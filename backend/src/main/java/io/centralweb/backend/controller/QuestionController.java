package io.centralweb.backend.controller;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/questions")
@Validated
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("")
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestBody @Valid QuestionCreateDTO question
    ) {
        QuestionDTO newQuestion = questionService.createQuestion(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(newQuestion);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable("questionId") UUID questionId) {
        QuestionDTO question = questionService.getQuestionById(questionId);
        return ResponseEntity.status(HttpStatus.OK).body(question);
    }

    @GetMapping("")
    public ResponseEntity<List<QuestionListDTO>> getAllPublishedQuestions() {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getAllPublishedQuestions());
    }

    @GetMapping("/{title}/title")
    public ResponseEntity<List<QuestionListDTO>> getQuestionsByTitle(
            @PathVariable("title") String title
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                questionService.getAllPublishedQuestionsByTitle(title)
        );
    }

    @GetMapping("/{technologyName}/tag")
    public ResponseEntity<List<QuestionListDTO>> getQuestionsByTag(
            @PathVariable String technologyName
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                questionService.getAllPublishedQuestionsByTechnologyName(technologyName)
        );
    }

    @GetMapping("/accepteds-answers")
    public ResponseEntity<List<QuestionListDTO>> getQuestionsWithAcceptedAnswers() {
        return ResponseEntity.status(HttpStatus.OK).body(
                questionService.getAllPublishedQuestionWithAcceptedAnswer()
        );
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable UUID questionId,
            @RequestBody @Valid QuestionUpdateDTO question
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                questionService.updateQuestion(questionId, question)
        );
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID questionId) {
        questionService.deleteQuestionById(questionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
