package io.centralweb.backend.mcp;

import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.service.AnswerService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class AnswerMCPTools {
    private final AnswerService answerService;

    public AnswerMCPTools(AnswerService answerService) {
        this.answerService = answerService;
    }

    @McpTool(name = "search_answers", description = "Busca respostas de uma pergunta publicada pelo ID da pergunta, incluindo a resposta aceita.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public McpSearch.Results<AnswerDTO> searchAnswers(
            @McpToolParam(description = "ID da pergunta publicada", required = true) UUID perguntaId,
            @McpToolParam(description = "Número da página, de 0 a 100; cada página contém até 5 resultados", required = true) int pagina) {
        if (perguntaId == null) throw new IllegalArgumentException("Informe o ID da pergunta.");
        return McpSearch.results(answerService.getAllAnswersFromQuestion(perguntaId, McpSearch.page(pagina)));
    }
}
