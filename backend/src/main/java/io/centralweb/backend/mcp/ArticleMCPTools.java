package io.centralweb.backend.mcp;

import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.service.ArticleService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class ArticleMCPTools {
    private final ArticleService articleService;

    public ArticleMCPTools(ArticleService articleService) {
        this.articleService = articleService;
    }

    @McpTool(name = "search_articles", description = "Busca artigos publicados por palavras-chave, com conteúdo e dados do autor.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public McpSearch.Results<ArticleDTO> searchArticles(
            @McpToolParam(description = "Termo de busca", required = true) String termo,
            @McpToolParam(description = "Número da página, de 0 a 100; cada página contém até 5 resultados", required = true) int pagina) {
        return McpSearch.results(articleService.searchPublishedArticles(McpSearch.term(termo), McpSearch.page(pagina)));
    }
}
