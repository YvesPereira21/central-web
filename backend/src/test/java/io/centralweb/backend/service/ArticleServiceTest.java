package io.centralweb.backend.service;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.ArticleCreateEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.ArticleMapper;
import io.centralweb.backend.model.*;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.ArticleRepository;
import io.centralweb.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private TagService tagService;
    @Mock
    private ApplicationEventPublisher publisher;
    @InjectMocks
    private ArticleService articleService;
    private User userAdmin;
    private User userPerson1;
    private User userPerson2;
    private Profile profilePerson1;
    private Profile profilePerson2;
    private Article article1;
    private ArticleDTO articleDTO;
    private ArticleCreateDTO articleCreateDTO;
    private Tag tagJava;
    private Tag tagRust;
    private TagDTO tagJavaDTO;
    private TagDTO tagRustDTO;
    private ProfileSimpleDTO profileSimpleDTO;

    @BeforeEach
    public void setUp() {
        userAdmin = new User();
        ReflectionTestUtils.setField(userAdmin, "userId", UUID.randomUUID());
        userAdmin.setEmail("testcentraldev@gmail.com");
        userAdmin.setPassword("password");
        userAdmin.setRole(UserRole.ADMIN);

        userPerson1 = new User();
        ReflectionTestUtils.setField(userPerson1, "userId", UUID.randomUUID());
        userPerson1.setEmail("testcentralweb@gmail.com");
        userPerson1.setPassword("password");
        userPerson1.setRole(UserRole.PERSON);

        userPerson2 = new User();
        ReflectionTestUtils.setField(userPerson2, "userId", UUID.randomUUID());
        userPerson2.setEmail("testcentraljunior@gmail.com");
        userPerson2.setPassword("password");
        userPerson2.setRole(UserRole.PERSON);

        profilePerson1 = new Profile();
        ReflectionTestUtils.setField(profilePerson1, "profileId", UUID.randomUUID());
        profilePerson1.setName("Usuário Teste");
        profilePerson1.setBio("Sou um programador de testes");
        profilePerson1.setUser(userPerson1);

        profilePerson2 = new Profile();
        ReflectionTestUtils.setField(profilePerson2, "profileId", UUID.randomUUID());
        profilePerson2.setName("Usuário Teste 2");
        profilePerson2.setBio("Sou um programador de testes 2");
        profilePerson2.setUser(userPerson2);

        profileSimpleDTO = new ProfileSimpleDTO(
                profilePerson1.getProfileId(),
                profilePerson1.getName(),
                profilePerson1.getExpertise(),
                profilePerson1.getLevel(),
                profilePerson1.isProfessional()
        );

        tagJava = new Tag();
        tagJava.setTechnologyName("Java");
        tagJava.setColor("#ED8B00");

        tagRust = new Tag();
        tagRust.setTechnologyName("Rust");
        tagRust.setColor("#ED7C10");

        tagJavaDTO = new TagDTO("Java", "#ED8B00");
        tagRustDTO = new TagDTO("Rust", "#ED7C10");

        article1 = new Article();
        ReflectionTestUtils.setField(article1, "articleId", UUID.randomUUID());
        article1.setTitle("Diferenças entre Python x Java");
        article1.setContent("Content 1");
        article1.setPublished(true);
        article1.setCreatedAt(LocalDate.now());
        article1.setTags(List.of(tagJava, tagRust));
        article1.setProfile(profilePerson1);

        articleCreateDTO = new ArticleCreateDTO(
                "Diferenças entre Python x Java",
                "Content 1",
                List.of("Java", "Rust")
        );

        articleDTO = new ArticleDTO(
                article1.getArticleId(),
                article1.getTitle(),
                article1.getContent(),
                article1.getCreatedAt(),
                List.of(tagJavaDTO, tagRustDTO),
                profileSimpleDTO,
                0L
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateArticle() {
        UUID userId = userPerson1.getUserId();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(tagService.convertTechnologyNamesToTags(articleCreateDTO.technologyNames()))
                .thenReturn(List.of(tagJava, tagRust));
        when(articleRepository.save(any(Article.class))).thenReturn(article1);
        when(articleMapper.toDTO(any(Article.class))).thenReturn(articleDTO);

        ArticleDTO result = articleService.createArticle(articleCreateDTO, userId);

        assertNotNull(result);
        assertEquals("Diferenças entre Python x Java", result.title());
        assertEquals("Content 1", result.content());
        assertEquals(List.of(tagJavaDTO, tagRustDTO), result.tags());
        assertEquals(LocalDate.now(), result.createdAt());
        assertEquals("Usuário Teste", result.profile().name());
        assertEquals("Iniciante", result.profile().level());
        assertFalse(result.profile().professional());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(tagService, times(1)).convertTechnologyNamesToTags(anyList());
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(articleMapper, times(1)).toDTO(any(Article.class));

        ArgumentCaptor<ArticleCreateEvent> eventCaptor = ArgumentCaptor.forClass(ArticleCreateEvent.class);

        verify(publisher, times(1)).publishEvent(eventCaptor.capture());

        ArticleCreateEvent capturedEvent = eventCaptor.getValue();

        assertEquals(profilePerson1.getProfileId(), capturedEvent.profileId());
    }

    @Test
    void shouldReturnArticle() {
        UUID articleId = article1.getArticleId();

        when(articleRepository.findById(article1.getArticleId())).thenReturn(Optional.of(article1));
        when(articleMapper.toDTO(any(Article.class))).thenReturn(articleDTO);

        ArticleDTO result = articleService.getArticleById(articleId);

        assertNotNull(result);
        assertEquals("Diferenças entre Python x Java", result.title());
        assertEquals("Content 1", result.content());
        assertEquals(List.of(tagJavaDTO, tagRustDTO), result.tags());
        assertEquals(LocalDate.now(), result.createdAt());
        assertEquals("Usuário Teste", result.profile().name());
        assertEquals("Iniciante", result.profile().level());
        assertFalse(result.profile().professional());
    }

    @Test
    void shouldReturnPublishedArticles() {
        List<Article> articles = List.of(article1);
        Page<Article> articlePage = new PageImpl<>(articles);

        when(articleRepository.findAllByPublishedIsTrue(any(Pageable.class))).thenReturn(articlePage);
        when(articleMapper.toDTO(any(Article.class))).thenReturn(articleDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleDTO> result = articleService.getAllPublishedArticles(pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(articleRepository, times(1)).findAllByPublishedIsTrue(any(Pageable.class));
        verify(articleMapper, times(1)).toDTO(any(Article.class));
    }

    @Test
    void shouldReturnPublishedArticlesByTitle() {
        String title = "Diferenças";
        List<Article> articles = List.of(article1);
        Page<Article> articlePage = new PageImpl<>(articles);

        when(articleRepository.findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(
                eq(title),
                any(Pageable.class)
        )).thenReturn(articlePage);
        when(articleMapper.toDTO(any(Article.class))).thenReturn(articleDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleDTO> result = articleService.getAllPublishedArticlesByTitle(title, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(articleRepository, times(1))
                .findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(eq(title), any(Pageable.class));
        verify(articleMapper, times(1)).toDTO(any(Article.class));
    }

    @Test
    void shouldReturnPublishedArticlesByTechnologyName() {
        String technologyName = "Rust";
        List<Article> articles = List.of(article1);
        Page<Article> articlePage = new PageImpl<>(articles);

        when(articleRepository.findAllByTags_TechnologyNameAndPublishedIsTrue(
                eq(technologyName),
                any(Pageable.class)
        )).thenReturn(articlePage);
        when(articleMapper.toDTO(any(Article.class))).thenReturn(articleDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleDTO> result = articleService.getAllPublishedArticlesByTechnologyName(technologyName, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(articleRepository, times(1))
                .findAllByTags_TechnologyNameAndPublishedIsTrue(eq(technologyName), any(Pageable.class));
        verify(articleMapper, times(1)).toDTO(any(Article.class));
    }

    @Test
    void shouldReturnPublishedArticlesByProfile() {
        UUID profileId = profilePerson1.getProfileId();
        List<Article> articles = List.of(article1);
        Page<Article> articlePage = new PageImpl<>(articles);

        when(articleRepository.findAllByProfile_ProfileIdAndPublishedIsTrue(
                eq(profileId),
                any(Pageable.class)
        )).thenReturn(articlePage);
        when(articleMapper.toDTO(any(Article.class))).thenReturn(articleDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleDTO> result = articleService.getAllPublishedArticlesByProfile(profileId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(articleRepository, times(1))
                .findAllByProfile_ProfileIdAndPublishedIsTrue(eq(profileId), any(Pageable.class));
        verify(articleMapper, times(1)).toDTO(any(Article.class));
    }

    @Test
    void shouldUpdateArticleWhenUserIsOwner() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = article1.getArticleId();
        ArticleUpdateDTO updateDTO = new ArticleUpdateDTO(
                "Título Atualizado no Teste",
                "Conteúdo Modificado",
                List.of("Java")
        );
        ArticleDTO updatedArticleDTO = new ArticleDTO(
                article1.getArticleId(),
                "Título Atualizado no Teste",
                "Conteúdo Modificado",
                article1.getCreatedAt(),
                List.of(tagJavaDTO),
                profileSimpleDTO,
                0L
        );

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));
        when(tagService.convertTechnologyNamesToTags(updateDTO.technologyNames()))
                .thenReturn(List.of(tagJava, tagRust));
        when(articleRepository.save(any(Article.class))).thenReturn(article1);
        when(articleMapper.toDTO(any(Article.class))).thenReturn(updatedArticleDTO);

        ArticleDTO result = articleService.updateArticle(articleId, updateDTO, userId);

        assertNotNull(result);
        assertEquals("Título Atualizado no Teste", result.title());
        assertEquals("Conteúdo Modificado", result.content());
        assertEquals(List.of(tagJavaDTO), result.tags());

        verify(articleRepository, times(1)).findById(articleId);
        verify(tagService, times(1)).convertTechnologyNamesToTags(updateDTO.technologyNames());
        verify(articleMapper, times(1)).updateArticleFromDTO(updateDTO, article1);
        verify(articleRepository, times(1)).save(article1);
        verify(articleMapper, times(1)).toDTO(article1);
    }

    @Test
    void shouldAddLikeWhenUserHasNotLiked() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = article1.getArticleId();

        article1.setArticleLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));

        assertDoesNotThrow(() -> articleService.toggleArticleLike(articleId, userId));
        assertTrue(article1.getArticleLikes().contains(profilePerson1));
        assertEquals(1, article1.getArticleLikes().size());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(article1);
    }

    @Test
    void shouldRemoveLikeWhenUserHasLiked() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = article1.getArticleId();

        List<Profile> likes = new ArrayList<>();
        likes.add(profilePerson1);
        article1.setArticleLikes(likes);

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));

        assertDoesNotThrow(() -> articleService.toggleArticleLike(articleId, userId));
        assertFalse(article1.getArticleLikes().contains(profilePerson1));
        assertEquals(0, article1.getArticleLikes().size());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(article1);
    }

    @Test
    void shouldDeleteArticleWhenUserIsOwner() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = article1.getArticleId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));

        assertDoesNotThrow(() -> articleService.deleteArticleById(articleId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).delete(article1);
    }

    @Test
    void shouldDeleteArticleWhenUserIsAdmin() {
        UUID userId = userAdmin.getUserId();
        UUID articleId = article1.getArticleId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));

        assertDoesNotThrow(() -> articleService.deleteArticleById(articleId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).delete(article1);
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateArticleAndThrowExceptionWhenProfileDoesNotExist(){
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.createArticle(articleCreateDTO, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(articleRepository, never()).save(any(Article.class));
        verify(articleMapper, never()).toDTO(any(Article.class));
    }

    @Test
    void shouldNotReturnArticleAndThrowExceptionWhenArticleDoesNotExist(){
        UUID articleId = UUID.randomUUID();

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.getArticleById(articleId)
        );

        verify(articleRepository, times(1)).findById(articleId);
        verify(articleMapper, never()).toDTO(any(Article.class));
    }

    @Test
    void shouldNotUpdateArticleAndThrowExceptionWhenArticleDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = UUID.randomUUID();
        ArticleUpdateDTO updateDTO = new ArticleUpdateDTO(
                "Título", "Conteúdo", List.of("Java")
        );

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.updateArticle(articleId, updateDTO, userId)
        );

        verify(articleRepository, times(1)).findById(articleId);
        verify(tagService, never()).convertTechnologyNamesToTags(anyList());
        verify(articleMapper, never()).updateArticleFromDTO(any(), any());
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldNotUpdateArticleAndThrowExceptionWhenUserIsNotOwner() {
        UUID userId = userPerson2.getUserId();
        UUID articleId = article1.getArticleId();
        ArticleUpdateDTO updateDTO = new ArticleUpdateDTO(
                "Título", "Conteúdo", List.of("Java")
        );

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> articleService.updateArticle(articleId, updateDTO, userId)
        );

        verify(articleRepository, times(1)).findById(articleId);
        verify(tagService, never()).convertTechnologyNamesToTags(anyList());
        verify(articleMapper, never()).updateArticleFromDTO(any(), any());
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldNotUpdateArticleAndThrowExceptionWhenTechnologyNameDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = article1.getArticleId();
        ArticleUpdateDTO updateDTO = new ArticleUpdateDTO(
                "Título", "Conteúdo", List.of("Go")
        );

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));
        when(tagService.convertTechnologyNamesToTags(anyList()))
                .thenThrow(new ObjectNotFoundException("Tag Go não encontrada"));

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.updateArticle(articleId, updateDTO, userId)
        );

        verify(articleRepository, times(1)).findById(articleId);
        verify(articleMapper, times(1)).updateArticleFromDTO(updateDTO, article1);
        verify(tagService, times(1)).convertTechnologyNamesToTags(anyList());
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldNotAddLikeAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID articleId = article1.getArticleId();

        article1.setArticleLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.toggleArticleLike(articleId, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(articleRepository, never()).findById(articleId);
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldNotAddLikeAndThrowExceptionWhenArticleDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID articleId = UUID.randomUUID();

        article1.setArticleLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.toggleArticleLike(articleId, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldNotDeleteArticleAndThrowExceptionWhenArticleDoesNotExist(){
        UUID userId = userPerson1.getUserId();
        UUID articleId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> articleService.deleteArticleById(articleId, userId)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).delete(any(Article.class));
    }

    @Test
    void shouldNotDeleteArticleAndThrowExceptionWhenUserIsNotOwnerAndIsNotAdmin() {
        UUID userId = userPerson2.getUserId();
        UUID articleId = article1.getArticleId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson2));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> articleService.deleteArticleById(articleId, userId)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).delete(any(Article.class));
    }
}