package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.ArticleControllerDoc;
import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.service.ArticleService;
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
@RequestMapping(path = "/articles")
@Validated
public class ArticleController implements ArticleControllerDoc {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("")
    public ResponseEntity<ArticleDTO> createArticle(
            @RequestBody ArticleCreateDTO article,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        ArticleDTO newArticle = articleService.createArticle(article, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @Override
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDTO> getArticle(@PathVariable UUID articleId) {
        return ResponseEntity.ok(articleService.getArticleById(articleId));
    }

    @Override
    @GetMapping("")
    public ResponseEntity<Page<ArticleDTO>> getAllPublishedArticles(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticles(pageable));
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<ArticleDTO>> searchPublishedArticles(
            @RequestParam("query") String query,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(articleService.searchPublishedArticles(query, pageable));
    }

    @Override
    @GetMapping("/{technologyName}/tag")
    public ResponseEntity<Page<ArticleDTO>> getArticlesByTag(
            @PathVariable String technologyName,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticlesByTechnologyName(technologyName, pageable));
    }

    @Override
    @GetMapping("/filter")
    public ResponseEntity<Page<ArticleDTO>> getArticlesByTags(
            @RequestParam List<String> tags,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticlesByTags(tags, pageable));
    }

    @Override
    @GetMapping("/{profileId}/profile")
    public ResponseEntity<Page<ArticleDTO>> getProfileArticles(
            @PathVariable UUID profileId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(articleService.getAllPublishedArticlesByProfile(profileId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{articleId}")
    public ResponseEntity<ArticleDTO> updateArticle(
            @PathVariable UUID articleId,
            @RequestBody ArticleUpdateDTO articleUpdated,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(articleService.updateArticle(articleId, articleUpdated, userId));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PatchMapping("/{articleId}/like")
    public ResponseEntity<Void> toggleArticleLike(
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userId
    ) {
        articleService.toggleArticleLike(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        articleService.deleteArticleById(articleId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
