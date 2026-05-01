package io.centralweb.backend.service;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.ArticleCreateEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.ArticleMapper;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ArticleRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TagService tagService;
    private final ApplicationEventPublisher publisher;

    public ArticleService(ArticleRepository articleRepository, ArticleMapper articleMapper, UserRepository userRepository, ProfileRepository profileRepository, TagService tagService, ApplicationEventPublisher publisher) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
        this.userRepository = userRepository;
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

    public Page<ArticleDTO> getAllPublishedArticles(Pageable pageable){
        return articleRepository.findAllByPublishedIsTrue(pageable)
                .map(articleMapper::toDTO);
    }

    public Page<ArticleDTO> getAllPublishedArticlesByTitle(String title, Pageable pageable) {
        return articleRepository
                .findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(title, pageable)
                .map(articleMapper::toDTO);
    }

    public Page<ArticleDTO> getAllPublishedArticlesByTechnologyName(String technologyName, Pageable pageable) {
        return articleRepository
                .findAllByTags_TechnologyNameAndPublishedIsTrue(technologyName, pageable)
                .map(articleMapper::toDTO);
    }

    public Page<ArticleDTO> getAllPublishedArticlesByProfile(UUID profileId, Pageable pageable) {
        return articleRepository
                .findAllByProfile_ProfileIdAndPublishedIsTrue(profileId, pageable)
                .map(articleMapper::toDTO);
    }

    public ArticleDTO updateArticle(UUID articleId, ArticleUpdateDTO articleUpdated, UUID userProfileId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if(!article.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
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

    public void deleteArticleById(UUID articleId, UUID userProfileId) {
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if(!article.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)){
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        articleRepository.delete(article);
    }
}
