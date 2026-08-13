package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.article.ArticleListSimpleCollectionDTO;
import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.dto.collection.CollectionDTO;
import io.centralweb.backend.dto.question.QuestionListSimpleCollectionDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.CollectionService;
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

@WebMvcTest(CollectionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private CollectionService collectionService;

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
    private UUID collectionId;
    private UUID articleId;
    private UUID questionId;

    private CollectionDTO collectionDTO;
    private CollectionCreateDTO collectionCreateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        collectionId = UUID.randomUUID();
        articleId = UUID.randomUUID();
        questionId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ArticleListSimpleCollectionDTO articleDTO = new ArticleListSimpleCollectionDTO(
                articleId,
                "Artigo Favorito",
                LocalDate.of(2026, 8, 13)
        );

        QuestionListSimpleCollectionDTO questionDTO = new QuestionListSimpleCollectionDTO(
                questionId,
                "Pergunta Favorita",
                LocalDate.of(2026, 8, 13),
                true
        );

        collectionDTO = new CollectionDTO(
                collectionId,
                "Minha Coleção",
                Set.of(articleDTO),
                Set.of(questionDTO)
        );

        collectionCreateDTO = new CollectionCreateDTO("Minha Coleção");
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateCollectionAndReturn201Created() throws Exception {
        when(collectionService.createCollection(any(CollectionCreateDTO.class), eq(userId))).thenReturn(collectionDTO);

        mockMvc.perform(post("/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(collectionCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.collectionId").value(collectionDTO.collectionId().toString()))
                .andExpect(jsonPath("$.name").value(collectionDTO.name()))
                .andExpect(jsonPath("$.articles[0].articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.articles[0].title").value("Artigo Favorito"))
                .andExpect(jsonPath("$.articles[0].createdAt").value("2026-08-13"))
                .andExpect(jsonPath("$.questions[0].questionId").value(questionId.toString()))
                .andExpect(jsonPath("$.questions[0].title").value("Pergunta Favorita"))
                .andExpect(jsonPath("$.questions[0].createdAt").value("2026-08-13"))
                .andExpect(jsonPath("$.questions[0].solutioned").value(true));
    }

    @Test
    void shouldGetMyCollectionsAndReturn200Ok() throws Exception {
        Page<CollectionDTO> page = new PageImpl<>(List.of(collectionDTO));
        when(collectionService.getMyCollections(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/collections/my-collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].collectionId").value(collectionDTO.collectionId().toString()))
                .andExpect(jsonPath("$.content[0].name").value(collectionDTO.name()))
                .andExpect(jsonPath("$.content[0].articles[0].articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.content[0].questions[0].questionId").value(questionId.toString()));
    }

    @Test
    void shouldGetCollectionByIdAndReturn200Ok() throws Exception {
        when(collectionService.getCollectionById(eq(collectionId), eq(userId))).thenReturn(collectionDTO);

        mockMvc.perform(get("/collections/{collectionId}", collectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collectionId").value(collectionDTO.collectionId().toString()))
                .andExpect(jsonPath("$.name").value(collectionDTO.name()));
    }

    @Test
    void shouldAddArticleToCollectionAndReturn204NoContent() throws Exception {
        doNothing().when(collectionService).addArticleToCollection(eq(collectionId), eq(articleId), eq(userId));

        mockMvc.perform(post("/collections/{collectionId}/articles/{articleId}", collectionId, articleId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAddQuestionToCollectionAndReturn204NoContent() throws Exception {
        doNothing().when(collectionService).addQuestionToCollection(eq(collectionId), eq(questionId), eq(userId));

        mockMvc.perform(post("/collections/{collectionId}/questions/{questionId}", collectionId, questionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRemoveArticleFromAllMyCollectionsAndReturn204NoContent() throws Exception {
        doNothing().when(collectionService).removeArticleFromAllMyCollections(eq(articleId), eq(userId));

        mockMvc.perform(delete("/collections/articles/{articleId}", articleId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRemoveQuestionFromAllMyCollectionsAndReturn204NoContent() throws Exception {
        doNothing().when(collectionService).removeQuestionFromAllMyCollections(eq(questionId), eq(userId));

        mockMvc.perform(delete("/collections/questions/{questionId}", questionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteCollectionAndReturn204NoContent() throws Exception {
        doNothing().when(collectionService).deleteCollectionById(eq(collectionId), eq(userId));

        mockMvc.perform(delete("/collections/{collectionId}", collectionId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateCollectionAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        CollectionCreateDTO invalidDTO = new CollectionCreateDTO("");

        mockMvc.perform(post("/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotGetCollectionByIdAndReturn404NotFoundWhenCollectionDoesNotExist() throws Exception {
        when(collectionService.getCollectionById(eq(collectionId), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Coleção não encontrada"));

        mockMvc.perform(get("/collections/{collectionId}", collectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetCollectionByIdAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(collectionService.getCollectionById(eq(collectionId), eq(userId)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono da coleção"));

        mockMvc.perform(get("/collections/{collectionId}", collectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAddArticleToCollectionAndReturn404NotFoundWhenCollectionOrArticleDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Coleção ou artigo não encontrado"))
                .when(collectionService).addArticleToCollection(eq(collectionId), eq(articleId), eq(userId));

        mockMvc.perform(post("/collections/{collectionId}/articles/{articleId}", collectionId, articleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAddArticleToCollectionAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono"))
                .when(collectionService).addArticleToCollection(eq(collectionId), eq(articleId), eq(userId));

        mockMvc.perform(post("/collections/{collectionId}/articles/{articleId}", collectionId, articleId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAddQuestionToCollectionAndReturn404NotFoundWhenCollectionOrQuestionDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Coleção ou pergunta não encontrada"))
                .when(collectionService).addQuestionToCollection(eq(collectionId), eq(questionId), eq(userId));

        mockMvc.perform(post("/collections/{collectionId}/questions/{questionId}", collectionId, questionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAddQuestionToCollectionAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono"))
                .when(collectionService).addQuestionToCollection(eq(collectionId), eq(questionId), eq(userId));

        mockMvc.perform(post("/collections/{collectionId}/questions/{questionId}", collectionId, questionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteCollectionAndReturn404NotFoundWhenCollectionDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Coleção não encontrada"))
                .when(collectionService).deleteCollectionById(eq(collectionId), eq(userId));

        mockMvc.perform(delete("/collections/{collectionId}", collectionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteCollectionAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono"))
                .when(collectionService).deleteCollectionById(eq(collectionId), eq(userId));

        mockMvc.perform(delete("/collections/{collectionId}", collectionId))
                .andExpect(status().isUnauthorized());
    }
}
