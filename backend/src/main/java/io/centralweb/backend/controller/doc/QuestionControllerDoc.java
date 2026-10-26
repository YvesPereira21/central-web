package io.centralweb.backend.controller.doc;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Question", description = "Gerenciamento de perguntas")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public interface QuestionControllerDoc {

    @Operation(summary = "Cria uma nova pergunta", description = "Cria uma pergunta associada ao usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pergunta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    ResponseEntity<QuestionDTO> createQuestion(
            @Valid QuestionCreateDTO question,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Busca uma pergunta por ID", description = "Retorna os detalhes de uma pergunta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pergunta encontrada"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada para o ID informado")
    })
    ResponseEntity<QuestionDTO> getQuestion(UUID questionId);

    @Operation(summary = "Lista todas as perguntas publicadas", description = "Retorna a lista de perguntas que estão publicadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas retornada com sucesso")
    })
    ResponseEntity<Page<QuestionListDTO>> getAllPublishedQuestions(Pageable pageable);

    @Operation(summary = "Busca perguntas por perfil", description = "Retorna as perguntas de um perfil específico, apenas para o próprio dono")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas do perfil"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    ResponseEntity<Page<QuestionListDTO>> getProfileQuestions(
            UUID profileId,
            Pageable pageable,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Busca perguntas pelo título ou conteúdo", description = "Pesquisa perguntas publicadas contendo o termo no título ou conteúdo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas correspondentes")
    })
    ResponseEntity<Page<QuestionListDTO>> searchPublishedQuestions(String query, Pageable pageable);

    @Operation(
            summary = "Busca perguntas por tag/tecnologia",
            description = "Retorna perguntas publicadas associadas a uma determinada tecnologia (tag)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas filtradas pela tecnologia")
    })
    ResponseEntity<Page<QuestionListDTO>> getQuestionsByTag(String technologyName, Pageable pageable);

    @Operation(
            summary = "Busca perguntas por múltiplas tags",
            description = "Retorna perguntas publicadas que possuam TODAS as tags listadas (Filtro Estrito)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas correspondentes")
    })
    ResponseEntity<Page<QuestionListDTO>> getQuestionsByTags(List<String> tags, Pageable pageable);

    @Operation(
            summary = "Lista perguntas com resposta aceita",
            description = "Retorna perguntas publicadas que possuem pelo menos uma resposta aceita"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perguntas com resposta aceita")
    })
    ResponseEntity<Page<QuestionListDTO>> getQuestionsWithAcceptedAnswer(Pageable pageable);

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
    ResponseEntity<QuestionDTO> updateQuestion(
            UUID questionId,
            @Valid QuestionUpdateDTO question,
            @Parameter(hidden = true) UUID userId
    );

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
    ResponseEntity<Void> toggleQuestionLike(
            UUID questionId,
            @Parameter(hidden = true) UUID userId
    );

    @Operation(summary = "Exclui uma pergunta", description = "Remove uma pergunta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pergunta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pergunta não encontrada")
    })
    ResponseEntity<Void> deleteQuestion(
            UUID questionId,
            @Parameter(hidden = true) UUID userProfileId
    );
}
