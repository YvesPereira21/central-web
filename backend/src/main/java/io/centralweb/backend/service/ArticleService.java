package io.centralweb.backend.service;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.events.ArticleCreateEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.mapper.ArticleMapper;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.repository.ArticleRepository;
import io.centralweb.backend.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher publisher;

    public ArticleService(ArticleRepository articleRepository, ArticleMapper articleMapper, ProfileRepository profileRepository, TagService tagService, ApplicationEventPublisher publisher) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
        this.profileRepository = profileRepository;
        this.tagService = tagService;
        this.publisher = publisher;
    }

    @Transactional(rollbackOn = Exception.class)
    public ArticleDTO createArticle(ArticleCreateDTO articleData, UUID userProfileId) {
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Article newArticle = new Article();
        newArticle.setTitle(articleData.title());
        newArticle.setContent(articleData.content());
        newArticle.setCreatedAt(LocalDate.now());
        newArticle.setTags(tagService.convertTechnologyNamesToTags(articleData.technologyNames()));
        newArticle.setPublished(true);
        newArticle.setProfile(profile);

        Article article = articleRepository.save(newArticle);
        publisher.publishEvent(new ArticleCreateEvent(profile.getProfileId()));

        return articleMapper.toDTO(article);
    }

    public ArticleDTO getArticleById(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

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
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if(!article.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new RuntimeException("Você não tem permissão para isso");
        }

        articleMapper.updateArticleFromDTO(articleUpdated, article);

        if (articleUpdated.technologyNames() != null) {
            article.setTags(tagService.convertTechnologyNamesToTags(articleUpdated.technologyNames()));
        }

        return articleMapper.toDTO(articleRepository.save(article));
    }

    public void toggleArticleLike(UUID articleId, UUID userProfileId) {
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if(article.getArticleLikes().contains(profile)) {
            article.removeLike(profile);
        } else {
            article.addLike(profile);
        }
        articleRepository.save(article);
    }

    public void deleteArticleById(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        articleRepository.delete(article);
    }
}
