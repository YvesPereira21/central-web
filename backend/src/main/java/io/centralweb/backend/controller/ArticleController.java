package io.centralweb.backend.controller;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/articles")
@Validated
public class ArticleController {
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("")
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody @Valid ArticleCreateDTO article){
        ArticleDTO newArticle = articleService.createArticle(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDTO> getArticle(@PathVariable UUID articleId){
        return ResponseEntity.status(HttpStatus.OK).body(articleService.getArticleById(articleId));
    }

    @GetMapping("")
    public ResponseEntity<List<ArticleDTO>> getAllPublishedArticles(){
        return ResponseEntity.status(HttpStatus.OK).body(articleService.getAllPublishedArticles());
    }

    @GetMapping("/{title}/title")
    public ResponseEntity<List<ArticleDTO>> getArticlesByTitle(
            @PathVariable("title") String title
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                articleService.getAllPublishedArticlesByTitle(title)
        );
    }

    @GetMapping("/{technologyName}/tag")
    public ResponseEntity<List<ArticleDTO>> getArticlesByTag(
            @PathVariable String technologyName
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                articleService.getAllPublishedArticlesByTechnologyName(technologyName)
        );
    }

    @GetMapping("/{profileId}/profile")
    public ResponseEntity<List<ArticleDTO>> getProfileArticles(
            @PathVariable UUID profileId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                articleService.getAllPublishedArticlesByProfile(profileId)
        );
    }

    @PutMapping("/{articleId}")
    public ResponseEntity<ArticleDTO> updateArticle(
            @PathVariable UUID articleId,
            @RequestBody @Valid ArticleUpdateDTO articleUpdated
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                articleService.updateArticle(articleId, articleUpdated)
        );
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID articleId) {
        articleService.deleteArticleById(articleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
