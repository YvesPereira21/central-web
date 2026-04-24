package io.centralweb.backend.service;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.mapper.ArticleMapper;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.repository.ArticleRepository;
import io.centralweb.backend.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final ProfileRepository profileRepository;
    private final TagService tagService;

    public ArticleService(ArticleRepository articleRepository, ArticleMapper articleMapper, ProfileRepository profileRepository, TagService tagService) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
        this.profileRepository = profileRepository;
        this.tagService = tagService;
    }

    public ArticleDTO createArticle(ArticleCreateDTO article, UUID userProfileId) {
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        Article newArticle = new Article();
        newArticle.setTitle(article.title());
        newArticle.setContent(article.content());
        newArticle.setCreatedAt(LocalDate.now());
        newArticle.setTags(tagService.convertTechnologyNamesToTags(article.technologyNames()));
        newArticle.setPublished(true);
        newArticle.setProfile(profile);

        return articleMapper.toDTO(articleRepository.save(newArticle));
    }

    public ArticleDTO getArticleById(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow();

        return articleMapper.toDTO(article);
    }

    public List<ArticleDTO> getAllPublishedArticles(){
        return articleRepository.findAllByPublishedIsTrue().stream()
                .map(articleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ArticleDTO> getAllPublishedArticlesByTitle(String title){
        return articleRepository
                .findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(title)
                .stream()
                .map(articleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ArticleDTO> getAllPublishedArticlesByTechnologyName(
            String technologyName
    ) {
        return articleRepository
                .findAllByTags_TechnologyNameAndPublishedIsTrue(technologyName)
                .stream()
                .map(articleMapper::toDTO)
                .toList();
    }

    public List<ArticleDTO> getAllPublishedArticlesByProfile(UUID profileId) {
        return articleRepository
                .findAllByProfile_ProfileIdAndPublishedIsTrue(profileId)
                .stream()
                .map(articleMapper::toDTO)
                .toList();
    }

    public ArticleDTO updateArticle(UUID articleId, ArticleUpdateDTO articleUpdated, UUID userProfileId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow();

        if(!article.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new RuntimeException();
        }

        articleMapper.updateArticleFromDTO(articleUpdated, article);

        if (articleUpdated.technologyNames() != null) {
            article.setTags(tagService.convertTechnologyNamesToTags(articleUpdated.technologyNames()));
        }

        return articleMapper.toDTO(articleRepository.save(article));
    }

    public void deleteArticleById(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow();

        articleRepository.delete(article);
    }
}
