package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.QuestionService;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private QuestionService questionService;

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
    private UUID questionId;
    private UUID profileId;

    private QuestionDTO questionDTO;
    private QuestionListDTO questionListDTO;
    private QuestionCreateDTO questionCreateDTO;
    private QuestionUpdateDTO questionUpdateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ProfileSimpleDTO profileSimpleDTO = new ProfileSimpleDTO(
                profileId,
                userId,
                "Bob Johnson",
                "Mid Level",
                true,
                "http://example.com/bob.jpg"
        );

        TagDTO tagDTO = new TagDTO("Java", "#FF0000");

        questionDTO = new QuestionDTO(
                questionId,
                "Como funciona o Spring Boot?",
                "Dúvida detalhada sobre a inicialização do Spring Boot.",
                true,
                false,
                LocalDate.of(2026, 8, 13),
                profileSimpleDTO,
                Set.of(tagDTO),
                Collections.emptySet(),
                15L,
                true,
                false
        );

        questionListDTO = new QuestionListDTO(
                questionId,
                "Como funciona o Spring Boot?",
                "Dúvida detalhada sobre a inicialização do Spring Boot.",
                true,
                false,
                LocalDate.of(2026, 8, 13),
                profileSimpleDTO,
                Set.of(tagDTO),
                15L,
                true,
                false
        );

        questionCreateDTO = new QuestionCreateDTO(
                "Como funciona o Spring Boot?",
                "Dúvida detalhada sobre a inicialização do Spring Boot.",
                Set.of("Java")
        );

        questionUpdateDTO = new QuestionUpdateDTO(
                "Como funciona o Spring Boot 3?",
                "Conteúdo atualizado da dúvida.",
                Set.of("Java", "Spring")
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateQuestionAndReturn201Created() throws Exception {
        when(questionService.createQuestion(any(QuestionCreateDTO.class), eq(userId))).thenReturn(questionDTO);

        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionId").value(questionDTO.questionId().toString()))
                .andExpect(jsonPath("$.title").value(questionDTO.title()))
                .andExpect(jsonPath("$.content").value(questionDTO.content()))
                .andExpect(jsonPath("$.published").value(questionDTO.published()))
                .andExpect(jsonPath("$.solutioned").value(questionDTO.solutioned()))
                .andExpect(jsonPath("$.createdAt").value(questionDTO.createdAt().toString()))
                .andExpect(jsonPath("$.profile.profileId").value(questionDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.tags[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.questionTotalLikes").value(questionDTO.questionTotalLikes()))
                .andExpect(jsonPath("$.liked").value(questionDTO.liked()))
                .andExpect(jsonPath("$.saved").value(questionDTO.saved()));
    }

    @Test
    void shouldGetQuestionAndReturn200Ok() throws Exception {
        when(questionService.getQuestionById(eq(questionId))).thenReturn(questionDTO);

        mockMvc.perform(get("/questions/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(questionDTO.questionId().toString()))
                .andExpect(jsonPath("$.title").value(questionDTO.title()))
                .andExpect(jsonPath("$.content").value(questionDTO.content()))
                .andExpect(jsonPath("$.published").value(questionDTO.published()))
                .andExpect(jsonPath("$.solutioned").value(questionDTO.solutioned()))
                .andExpect(jsonPath("$.createdAt").value(questionDTO.createdAt().toString()))
                .andExpect(jsonPath("$.profile.profileId").value(questionDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.tags[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.questionTotalLikes").value(questionDTO.questionTotalLikes()))
                .andExpect(jsonPath("$.liked").value(questionDTO.liked()))
                .andExpect(jsonPath("$.saved").value(questionDTO.saved()));
    }

    @Test
    void shouldGetAllPublishedQuestionsAndReturn200Ok() throws Exception {
        Page<QuestionListDTO> page = new PageImpl<>(List.of(questionListDTO));
        when(questionService.getAllPublishedQuestions(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionId").value(questionListDTO.questionId().toString()))
                .andExpect(jsonPath("$.content[0].title").value(questionListDTO.title()))
                .andExpect(jsonPath("$.content[0].content").value(questionListDTO.content()))
                .andExpect(jsonPath("$.content[0].published").value(questionListDTO.published()))
                .andExpect(jsonPath("$.content[0].solutioned").value(questionListDTO.solutioned()))
                .andExpect(jsonPath("$.content[0].createdAt").value(questionListDTO.createdAt().toString()))
                .andExpect(jsonPath("$.content[0].profile.profileId").value(questionListDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.content[0].tags[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.content[0].questionTotalLikes").value(questionListDTO.questionTotalLikes()))
                .andExpect(jsonPath("$.content[0].liked").value(questionListDTO.liked()))
                .andExpect(jsonPath("$.content[0].saved").value(questionListDTO.saved()));
    }

    @Test
    void shouldGetProfileQuestionsAndReturn200Ok() throws Exception {
        Page<QuestionListDTO> page = new PageImpl<>(List.of(questionListDTO));
        when(questionService.getAllQuestionsByProfile(eq(profileId), eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/questions/{profileId}/profile", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionId").value(questionListDTO.questionId().toString()));
    }

    @Test
    void shouldSearchPublishedQuestionsAndReturn200Ok() throws Exception {
        Page<QuestionListDTO> page = new PageImpl<>(List.of(questionListDTO));
        when(questionService.searchPublishedQuestions(eq("Spring"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/questions/search")
                        .param("query", "Spring")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionId").value(questionListDTO.questionId().toString()));
    }

    @Test
    void shouldGetQuestionsByTagAndReturn200Ok() throws Exception {
        Page<QuestionListDTO> page = new PageImpl<>(List.of(questionListDTO));
        when(questionService.getAllPublishedQuestionsByTechnologyName(eq("Java"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/questions/{technologyName}/tag", "Java")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionId").value(questionListDTO.questionId().toString()));
    }

    @Test
    void shouldGetQuestionsByTagsAndReturn200Ok() throws Exception {
        Page<QuestionListDTO> page = new PageImpl<>(List.of(questionListDTO));
        when(questionService.getAllPublishedQuestionsByTags(eq(List.of("Java", "Spring")), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/questions/filter")
                        .param("tags", "Java", "Spring")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionId").value(questionListDTO.questionId().toString()));
    }

    @Test
    void shouldGetQuestionsWithAcceptedAnswerAndReturn200Ok() throws Exception {
        Page<QuestionListDTO> page = new PageImpl<>(List.of(questionListDTO));
        when(questionService.getAllPublishedQuestionWithAcceptedAnswer(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/questions/accepteds-answers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].questionId").value(questionListDTO.questionId().toString()));
    }

    @Test
    void shouldUpdateQuestionAndReturn200Ok() throws Exception {
        when(questionService.updateQuestion(eq(questionId), any(QuestionUpdateDTO.class), eq(userId))).thenReturn(questionDTO);

        mockMvc.perform(put("/questions/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(questionDTO.questionId().toString()))
                .andExpect(jsonPath("$.title").value(questionDTO.title()));
    }

    @Test
    void shouldToggleQuestionLikeAndReturn200Ok() throws Exception {
        doNothing().when(questionService).toggleQuestionLike(eq(questionId), eq(userId));

        mockMvc.perform(patch("/questions/{questionId}/like", questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteQuestionAndReturn204NoContent() throws Exception {
        doNothing().when(questionService).deleteQuestionById(eq(questionId), eq(userId));

        mockMvc.perform(delete("/questions/{questionId}", questionId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateQuestionAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        QuestionCreateDTO invalidDTO = new QuestionCreateDTO("", "", Set.of());

        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateQuestionAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(questionService.createQuestion(any(QuestionCreateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionCreateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetQuestionAndReturn404NotFoundWhenQuestionDoesNotExist() throws Exception {
        when(questionService.getQuestionById(eq(questionId)))
                .thenThrow(new ObjectNotFoundException("Pergunta não encontrada"));

        mockMvc.perform(get("/questions/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetProfileQuestionsAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(questionService.getAllQuestionsByProfile(eq(profileId), eq(userId), any(Pageable.class)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/questions/{profileId}/profile", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetProfileQuestionsAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(questionService.getAllQuestionsByProfile(eq(profileId), eq(userId), any(Pageable.class)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono do perfil"));

        mockMvc.perform(get("/questions/{profileId}/profile", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotUpdateQuestionAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        QuestionUpdateDTO invalidDTO = new QuestionUpdateDTO("Title", "Content", Set.of("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8"));

        mockMvc.perform(put("/questions/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateQuestionAndReturn404NotFoundWhenQuestionDoesNotExist() throws Exception {
        when(questionService.updateQuestion(eq(questionId), any(QuestionUpdateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Pergunta não encontrada"));

        mockMvc.perform(put("/questions/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateQuestionAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(questionService.updateQuestion(eq(questionId), any(QuestionUpdateDTO.class), eq(userId)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não é o autor"));

        mockMvc.perform(put("/questions/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotToggleQuestionLikeAndReturn404NotFoundWhenQuestionDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Pergunta não encontrada"))
                .when(questionService).toggleQuestionLike(eq(questionId), eq(userId));

        mockMvc.perform(patch("/questions/{questionId}/like", questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteQuestionAndReturn404NotFoundWhenQuestionDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Pergunta não encontrada"))
                .when(questionService).deleteQuestionById(eq(questionId), eq(userId));

        mockMvc.perform(delete("/questions/{questionId}", questionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteQuestionAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o autor"))
                .when(questionService).deleteQuestionById(eq(questionId), eq(userId));

        mockMvc.perform(delete("/questions/{questionId}", questionId))
                .andExpect(status().isUnauthorized());
    }
}
