package io.centralweb.backend.controller;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.centralweb.backend.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/questions")
@Validated
@Tag(name = "Question", description = "Gerenciamento de perguntas")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("")
    @Operation(summary = "Cria uma nova pergunta", description = "Cria uma pergunta associada ao usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pergunta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestBody @Valid QuestionCreateDTO question,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        QuestionDTO newQuestion = questionService.createQuestion(question, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newQuestion);
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "Busca uma pergunta por ID", description = "Retorna os detalhes de uma pergunta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pergunta encontrada"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada para o ID informado")
    })
    public ResponseEntity<QuestionDTO> getQuestion(
            @PathVariable("questionId") UUID questionId
    ) {
        QuestionDTO question = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(question);
    }

    @GetMapping("")
    @Operation(summary = "Lista todas as perguntas publicadas", description = "Retorna a lista de perguntas que estão publicadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas retornada com sucesso")
    })
    public ResponseEntity<Page<QuestionListDTO>> getAllPublishedQuestions(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestions(pageable));
    }

    @GetMapping("/{title}/title")
    @Operation(summary = "Busca perguntas pelo título", description = "Pesquisa perguntas publicadas cujo título contenha o texto informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas correspondentes")
    })
    public ResponseEntity<Page<QuestionListDTO>> getQuestionsByTitle(
            @PathVariable("title") String title,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestionsByTitle(
                title, pageable
        ));
    }

    @GetMapping("/{technologyName}/tag")
    @Operation(
            summary = "Busca perguntas por tag/tecnologia",
            description = "Retorna perguntas publicadas associadas a uma determinada tecnologia (tag)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas filtradas pela tecnologia")
    })
    public ResponseEntity<Page<QuestionListDTO>> getQuestionsByTag(
            @PathVariable String technologyName,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestionsByTechnologyName(
                technologyName, pageable
        ));
    }

    @GetMapping("/accepteds-answers")
    @Operation(
            summary = "Lista perguntas com resposta aceita",
            description = "Retorna perguntas publicadas que possuem pelo menos uma resposta aceita"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas com resposta aceita")
    })
    public ResponseEntity<Page<QuestionListDTO>> getQuestionsWithAcceptedAnswer(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getAllPublishedQuestionWithAcceptedAnswer(
                pageable
        ));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{questionId}")
    @Operation(
            summary = "Atualiza uma pergunta",
            description = "Atualiza os dados de uma pergunta existente. Apenas o autor pode alterá-la."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pergunta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado usuário não é o autor")
    })
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable UUID questionId,
            @RequestBody @Valid QuestionUpdateDTO question,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.updateQuestion(questionId, question, userId));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{questionId}/like")
    @Operation(
            summary = "Adiciona/Remove curtida",
            description = "Adiciona/Remove uma curtida para uma pergunta existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Usuário não está autenticado")
    })
    public ResponseEntity<Void> toggleQuestionLike(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userId
    ) {
        questionService.toggleQuestionLike(questionId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{questionId}")
    @Operation(summary = "Exclui uma pergunta", description = "Remove uma pergunta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pergunta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada")
    })
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        questionService.deleteQuestionById(questionId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}