package io.centralweb.backend.mcp;

import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.service.CommentService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CommentMCPTools {
    private final CommentService commentService;

    public CommentMCPTools(CommentService commentService) {
        this.commentService = commentService;
    }

    @McpTool(name = "search_comments", description = "Busca comentários pelo ID de uma resposta vinculada a uma pergunta publicada.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public McpSearch.Results<CommentDTO> searchComments(
            @McpToolParam(description = "ID da resposta", required = true) UUID respostaId,
            @McpToolParam(description = "Número da página, de 0 a 100; cada página contém até 5 resultados", required = true) int pagina) {
        if (respostaId == null) throw new IllegalArgumentException("Informe o ID da resposta.");
        return McpSearch.results(commentService.getCommentsFromPublishedQuestion(respostaId, McpSearch.page(pagina)));
    }
}
