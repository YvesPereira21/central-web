package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    private UUID userId;
    private UUID articleId;
    private UUID profileId;
    private ArticleDTO articleDTO;
    private ArticleCreateDTO articleCreateDTO;
    private ArticleUpdateDTO articleUpdateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        articleId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ProfileSimpleDTO profileSimpleDTO = new ProfileSimpleDTO(
                profileId,
                userId,
                "Jane Doe",
                "Senior",
                true,
                "http://example.com/photo.jpg"
        );

        TagDTO tagDTO = new TagDTO("Java", "#FF0000");

        articleDTO = new ArticleDTO(
                articleId,
                "Título do Artigo",
                "Conteúdo completo do artigo de teste.",
                LocalDate.of(2026, 8, 13),
                Set.of(tagDTO),
                profileSimpleDTO,
                25L,
                true,
                false
        );

        articleCreateDTO = new ArticleCreateDTO("Título do Artigo", "Conteúdo completo do artigo de teste.", Set.of("Java"));
        articleUpdateDTO = new ArticleUpdateDTO("Título Atualizado", "Conteúdo Atualizado", Set.of("Java", "Spring"));
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateArticleAndReturn201Created() throws Exception {
        when(articleService.createArticle(any(ArticleCreateDTO.class), eq(userId))).thenReturn(articleDTO);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.articleId").value(articleDTO.articleId().toString()))
                .andExpect(jsonPath("$.title").value(articleDTO.title()))
                .andExpect(jsonPath("$.content").value(articleDTO.content()))
                .andExpect(jsonPath("$.createdAt").value(articleDTO.createdAt().toString()))
                .andExpect(jsonPath("$.tags[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.tags[0].color").value("#FF0000"))
                .andExpect(jsonPath("$.profile.profileId").value(articleDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.profile.userId").value(articleDTO.profile().userId().toString()))
                .andExpect(jsonPath("$.profile.name").value(articleDTO.profile().name()))
                .andExpect(jsonPath("$.profile.level").value(articleDTO.profile().level()))
                .andExpect(jsonPath("$.profile.professional").value(articleDTO.profile().professional()))
                .andExpect(jsonPath("$.profile.photoUrl").value(articleDTO.profile().photoUrl()))
                .andExpect(jsonPath("$.articleTotalLikes").value(articleDTO.articleTotalLikes()))
                .andExpect(jsonPath("$.liked").value(articleDTO.liked()))
                .andExpect(jsonPath("$.saved").value(articleDTO.saved()));
    }

    @Test
    void shouldGetArticleAndReturn200Ok() throws Exception {
        when(articleService.getArticleById(articleId)).thenReturn(articleDTO);

        mockMvc.perform(get("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(articleDTO.articleId().toString()))
                .andExpect(jsonPath("$.title").value(articleDTO.title()))
                .andExpect(jsonPath("$.content").value(articleDTO.content()))
                .andExpect(jsonPath("$.createdAt").value(articleDTO.createdAt().toString()))
                .andExpect(jsonPath("$.tags[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.tags[0].color").value("#FF0000"))
                .andExpect(jsonPath("$.profile.profileId").value(articleDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.profile.userId").value(articleDTO.profile().userId().toString()))
                .andExpect(jsonPath("$.profile.name").value(articleDTO.profile().name()))
                .andExpect(jsonPath("$.profile.level").value(articleDTO.profile().level()))
                .andExpect(jsonPath("$.profile.professional").value(articleDTO.profile().professional()))
                .andExpect(jsonPath("$.profile.photoUrl").value(articleDTO.profile().photoUrl()))
                .andExpect(jsonPath("$.articleTotalLikes").value(articleDTO.articleTotalLikes()))
                .andExpect(jsonPath("$.liked").value(articleDTO.liked()))
                .andExpect(jsonPath("$.saved").value(articleDTO.saved()));
    }

    @Test
    void shouldGetAllPublishedArticlesAndReturn200Ok() throws Exception {
        Page<ArticleDTO> page = new PageImpl<>(List.of(articleDTO));
        when(articleService.getAllPublishedArticles(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/articles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId").value(articleDTO.articleId().toString()))
                .andExpect(jsonPath("$.content[0].title").value(articleDTO.title()))
                .andExpect(jsonPath("$.content[0].content").value(articleDTO.content()))
                .andExpect(jsonPath("$.content[0].createdAt").value(articleDTO.createdAt().toString()))
                .andExpect(jsonPath("$.content[0].tags[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.content[0].tags[0].color").value("#FF0000"))
                .andExpect(jsonPath("$.content[0].profile.profileId").value(articleDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.content[0].profile.userId").value(articleDTO.profile().userId().toString()))
                .andExpect(jsonPath("$.content[0].profile.name").value(articleDTO.profile().name()))
                .andExpect(jsonPath("$.content[0].profile.level").value(articleDTO.profile().level()))
                .andExpect(jsonPath("$.content[0].profile.professional").value(articleDTO.profile().professional()))
                .andExpect(jsonPath("$.content[0].profile.photoUrl").value(articleDTO.profile().photoUrl()))
                .andExpect(jsonPath("$.content[0].articleTotalLikes").value(articleDTO.articleTotalLikes()))
                .andExpect(jsonPath("$.content[0].liked").value(articleDTO.liked()))
                .andExpect(jsonPath("$.content[0].saved").value(articleDTO.saved()));
    }

    @Test
    void shouldSearchPublishedArticlesAndReturn200Ok() throws Exception {
        Page<ArticleDTO> page = new PageImpl<>(List.of(articleDTO));
        when(articleService.searchPublishedArticles(eq("Java"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/articles/search")
                        .param("query", "Java")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId").value(articleDTO.articleId().toString()))
                .andExpect(jsonPath("$.content[0].title").value(articleDTO.title()));
    }

    @Test
    void shouldGetArticlesByTagAndReturn200Ok() throws Exception {
        Page<ArticleDTO> page = new PageImpl<>(List.of(articleDTO));
        when(articleService.getAllPublishedArticlesByTechnologyName(eq("Java"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/articles/{technologyName}/tag", "Java")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId").value(articleDTO.articleId().toString()));
    }

    @Test
    void shouldGetArticlesByTagsAndReturn200Ok() throws Exception {
        Page<ArticleDTO> page = new PageImpl<>(List.of(articleDTO));
        when(articleService.getAllPublishedArticlesByTags(eq(List.of("Java", "Spring")), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/articles/filter")
                        .param("tags", "Java", "Spring")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId").value(articleDTO.articleId().toString()));
    }

    @Test
    void shouldGetProfileArticlesAndReturn200Ok() throws Exception {
        Page<ArticleDTO> page = new PageImpl<>(List.of(articleDTO));
        when(articleService.getAllPublishedArticlesByProfile(eq(profileId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/articles/{profileId}/profile", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId").value(articleDTO.articleId().toString()));
    }

    @Test
    void shouldUpdateArticleAndReturn200Ok() throws Exception {
        when(articleService.updateArticle(eq(articleId), any(ArticleUpdateDTO.class), eq(userId))).thenReturn(articleDTO);

        mockMvc.perform(put("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(articleDTO.articleId().toString()))
                .andExpect(jsonPath("$.title").value(articleDTO.title()))
                .andExpect(jsonPath("$.content").value(articleDTO.content()));
    }

    @Test
    void shouldToggleArticleLikeAndReturn200Ok() throws Exception {
        doNothing().when(articleService).toggleArticleLike(eq(articleId), eq(userId));

        mockMvc.perform(patch("/articles/{articleId}/like", articleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteArticleAndReturn204NoContent() throws Exception {
        doNothing().when(articleService).deleteArticleById(eq(articleId), eq(userId));

        mockMvc.perform(delete("/articles/{articleId}", articleId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateArticleAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        ArticleCreateDTO invalidDTO = new ArticleCreateDTO("", "", Set.of());

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateArticleAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(articleService.createArticle(any(ArticleCreateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleCreateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetArticleAndReturn404NotFoundWhenArticleDoesNotExist() throws Exception {
        when(articleService.getArticleById(articleId))
                .thenThrow(new ObjectNotFoundException("Artigo não encontrado"));

        mockMvc.perform(get("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetProfileArticlesAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(articleService.getAllPublishedArticlesByProfile(eq(profileId), any(Pageable.class)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/articles/{profileId}/profile", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateArticleAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        ArticleUpdateDTO invalidDTO = new ArticleUpdateDTO("Title", "Content", Set.of("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8"));

        mockMvc.perform(put("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateArticleAndReturn404NotFoundWhenArticleDoesNotExist() throws Exception {
        when(articleService.updateArticle(eq(articleId), any(ArticleUpdateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Artigo não encontrado"));

        mockMvc.perform(put("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateArticleAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(articleService.updateArticle(eq(articleId), any(ArticleUpdateDTO.class), eq(userId)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não é o autor"));

        mockMvc.perform(put("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleUpdateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotToggleArticleLikeAndReturn404NotFoundWhenArticleDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Artigo não encontrado"))
                .when(articleService).toggleArticleLike(eq(articleId), eq(userId));

        mockMvc.perform(patch("/articles/{articleId}/like", articleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteArticleAndReturn404NotFoundWhenArticleDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Artigo não encontrado"))
                .when(articleService).deleteArticleById(eq(articleId), eq(userId));

        mockMvc.perform(delete("/articles/{articleId}", articleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteArticleAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o autor"))
                .when(articleService).deleteArticleById(eq(articleId), eq(userId));

        mockMvc.perform(delete("/articles/{articleId}", articleId))
                .andExpect(status().isUnauthorized());
    }
}
