package io.centralweb.backend.service;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.event.ArticleCreateEvent;
import io.centralweb.backend.event.ArticleDeleteEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.dto.mapper.ArticleMapper;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ArticleRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.repository.specification.GenericSearchSpecification;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
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
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleDTO createArticle(ArticleCreateDTO articleData, UUID userProfileId) {
        log.info("Criando novo artigo com o título: '{}' para o perfil de usuário com ID: '{}'", articleData.title(), userProfileId);
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
        log.info("Artigo criado com sucesso com o ID: '{}' para o perfil com ID: '{}'", article.getArticleId(), profile.getProfileId());
        publisher.publishEvent(new ArticleCreateEvent(profile.getProfileId()));

        return articleMapper.toDTO(article);
    }

    @Cacheable(value = "articles", key = "#articleId")
    public ArticleDTO getArticleById(UUID articleId) {
        log.debug("Buscando artigo por ID: {}", articleId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        return articleMapper.toDTO(article);
    }

    @Cacheable(value = "articles")
    public Page<ArticleDTO> getAllPublishedArticles(Pageable pageable){
        log.debug("Buscando página {} de artigos publicados", pageable.getPageNumber());
        return articleRepository.findAllByPublishedIsTrue(pageable)
                .map(articleMapper::toDTO);
    }

    @Cacheable(value = "articles")
    public Page<ArticleDTO> searchPublishedArticles(String keyword, Pageable pageable) {
        Specification<Article> spec = GenericSearchSpecification.searchByTitleOrContent(keyword);
        
        return articleRepository.findAll(spec, pageable).map(articleMapper::toDTO);
    }

    @Cacheable(value = "articles")
    public Page<ArticleDTO> getAllPublishedArticlesByTechnologyName(String technologyName, Pageable pageable) {
        return articleRepository
                .findAllByTags_TechnologyNameAndPublishedIsTrue(technologyName, pageable)
                .map(articleMapper::toDTO);
    }

    @Cacheable(value = "articles")
    public Page<ArticleDTO> getAllPublishedArticlesByTags(List<String> tags, Pageable pageable) {
        if (tags == null || tags.isEmpty()) {
            return getAllPublishedArticles(pageable);
        }
        return articleRepository
                .findAllByTagsStrict(tags, tags.size(), pageable)
                .map(articleMapper::toDTO);
    }

    @Cacheable(value = "articles")
    public Page<ArticleDTO> getAllPublishedArticlesByProfile(UUID profileId, Pageable pageable) {
        return articleRepository
                .findAllByProfile_ProfileIdAndPublishedIsTrue(profileId, pageable)
                .map(articleMapper::toDTO);
    }

    @CacheEvict(value = "articles", allEntries = true)
    public ArticleDTO updateArticle(UUID articleId, ArticleUpdateDTO articleUpdated, UUID userProfileId) {
        log.info("Atualizando artigo com ID: '{}' solicitado pelo perfil de usuário com ID: '{}'", articleId, userProfileId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if(!article.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        articleMapper.updateArticleFromDTO(articleUpdated, article);

        if (articleUpdated.technologyNames() != null) {
            article.setTags(tagService.convertTechnologyNamesToTags(articleUpdated.technologyNames()));
        }

        Article savedArticle = articleRepository.save(article);
        log.info("Artigo com ID: '{}' atualizado com sucesso", articleId);
        return articleMapper.toDTO(savedArticle);
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

    @CacheEvict(value = "articles", allEntries = true)
    public void deleteArticleById(UUID articleId, UUID userProfileId) {
        log.info("Excluindo artigo com ID: '{}' solicitado pelo perfil de usuário com ID: '{}'", articleId, userProfileId);
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if(!article.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)){
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        articleRepository.delete(article);
        log.info("Artigo com ID: '{}' excluído com sucesso", articleId);

        publisher.publishEvent(new ArticleDeleteEvent(article.getProfile().getProfileId()));
    }
}
