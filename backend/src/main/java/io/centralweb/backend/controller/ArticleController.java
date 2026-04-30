package io.centralweb.backend.controller;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.security.SecurityConfigurations;
import io.centralweb.backend.service.ArticleService;
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
@RequestMapping(path = "/articles")
@Validated
@Tag(name = "Article", description = "Gerenciamento de artigos")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("")
    @Operation(summary = "Cria um artigo", description = "Cria um novo artigo associado ao usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artigo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<ArticleDTO> createArticle(
            @RequestBody @Valid ArticleCreateDTO article,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        ArticleDTO newArticle = articleService.createArticle(article, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @GetMapping("/{articleId}")
    @Operation(summary = "Busca artigo por ID", description = "Retorna os detalhes de um artigo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artigo encontrado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado")
    })
    public ResponseEntity<ArticleDTO> getArticle(@PathVariable UUID articleId) {
        return ResponseEntity.ok(articleService.getArticleById(articleId));
    }

    @GetMapping("")
    @Operation(summary = "Lista todos os artigos publicados", description = "Retorna a lista de artigos publicados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artigos retornada com sucesso")
    })
    public ResponseEntity<List<ArticleDTO>> getAllPublishedArticles() {
        return ResponseEntity.ok(articleService.getAllPublishedArticles());
    }

    @GetMapping("/{title}/title")
    @Operation(summary = "Busca artigos pelo título", description = "Pesquisa artigos publicados cujo título contenha o texto informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artigos correspondentes")
    })
    public ResponseEntity<List<ArticleDTO>> getArticlesByTitle(
            @PathVariable("title") String title
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticlesByTitle(title));
    }

    @GetMapping("/{technologyName}/tag")
    @Operation(summary = "Busca artigos por tag/tecnologia", description = "Retorna artigos publicados associados a uma tecnologia (tag)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artigos filtrados pela tecnologia")
    })
    public ResponseEntity<List<ArticleDTO>> getArticlesByTag(
            @PathVariable String technologyName
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticlesByTechnologyName(technologyName));
    }

    @GetMapping("/{profileId}/profile")
    @Operation(summary = "Busca artigos por perfil", description = "Retorna os artigos publicados de um perfil específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artigos do perfil"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<List<ArticleDTO>> getProfileArticles(
            @PathVariable UUID profileId
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticlesByProfile(profileId));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{articleId}")
    @Operation(summary = "Atualiza um artigo", description = "Atualiza um artigo existente. Apenas o autor pode alterá-lo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artigo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o autor)")
    })
    public ResponseEntity<ArticleDTO> updateArticle(
            @PathVariable UUID articleId,
            @RequestBody @Valid ArticleUpdateDTO articleUpdated,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(articleService.updateArticle(articleId, articleUpdated, userId));
    }

    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{articleId}/like")
    @Operation(
            summary = "Adiciona/Remove curtida",
            description = "Adiciona/Remove uma curtida para um artigo existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Usuário não está autenticado")
    })
    public ResponseEntity<Void> toggleArticleLike(
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userId
    ) {
        articleService.toggleArticleLike(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{articleId}")
    @Operation(summary = "Exclui um artigo", description = "Remove um artigo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artigo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado")
    })
    public ResponseEntity<Void> deleteArticle(
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        articleService.deleteArticleById(articleId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
