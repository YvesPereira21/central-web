package io.centralweb.backend.mcp;

import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.service.QuestionService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class QuestionMCPTools {
    private final QuestionService questionService;

    public QuestionMCPTools(QuestionService questionService) {
        this.questionService = questionService;
    }

    @McpTool(name = "search_questions", description = "Busca perguntas publicadas por palavras-chave, com conteúdo e dados do autor.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public McpSearch.Results<QuestionListDTO> searchQuestions(
            @McpToolParam(description = "Termo de busca", required = true) String termo,
            @McpToolParam(description = "Número da página, de 0 a 100; cada página contém até 5 resultados", required = true) int pagina) {
        return McpSearch.results(questionService.searchPublishedQuestions(McpSearch.term(termo), McpSearch.page(pagina)));
    }
}
