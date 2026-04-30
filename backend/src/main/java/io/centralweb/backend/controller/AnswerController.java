package io.centralweb.backend.controller;

import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.centralweb.backend.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/answers")
@Validated
@Tag(name = "Answer", description = "Gerenciamento de respostas")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("")
    @Operation(summary = "Cria uma resposta", description = "Cria uma nova resposta associada ao usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resposta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<AnswerDTO> createAnswer(
            @RequestBody @Valid AnswerCreateDTO answer,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        AnswerDTO newAwnser = answerService.createAnswer(answer, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAwnser);
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "Lista respostas de uma pergunta", description = "Retorna todas as respostas associadas a uma pergunta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de respostas retornada"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada")
    })
    public ResponseEntity<List<AnswerDTO>> getAllAnswersFromQuestion(@PathVariable UUID questionId) {
        return ResponseEntity.ok(answerService.getAllAnswersFromQuestion(questionId));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{answerId}")
    @Operation(summary = "Aceita uma resposta", description = "Marca uma resposta como aceita para uma pergunta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resposta aceita com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não autorizado)"),
            @ApiResponse(responseCode = "404", description = "Resposta ou pergunta não encontrada")
    })
    public ResponseEntity<Void> acceptAnswer(
            @PathVariable UUID answerId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        answerService.acceptAnswer(answerId, userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{answerId}/like")
    @Operation(
            summary = "Adiciona/Remove curtida",
            description = "Adiciona/Remove uma curtida para uma resposta existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Usuário não está autenticado")
    })
    public ResponseEntity<Void> toggleAnswerLike(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userId
    ) {
        answerService.toggleAnswerLike(answerId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{answerId}")
    @Operation(summary = "Exclui uma resposta", description = "Remove uma resposta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resposta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada")
    })
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        answerService.deleteAnswerById(answerId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
