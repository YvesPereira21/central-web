package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.exception.QuestionHaveAnswerAlreadyAcceptedException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.AnswerService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnswerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private AnswerService answerService;

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
    private UUID answerId;
    private UUID profileId;
    private AnswerDTO answerDTO;
    private AnswerCreateDTO answerCreateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        answerId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ProfileSimpleDTO profileSimpleDTO = new ProfileSimpleDTO(
                profileId,
                userId,
                "John Doe",
                "Senior",
                true,
                "http://example.com/photo.jpg"
        );

        answerDTO = new AnswerDTO(
                answerId,
                "Esta é uma resposta de teste",
                false,
                LocalDate.of(2026, 8, 13),
                profileSimpleDTO,
                10L,
                true
        );

        answerCreateDTO = new AnswerCreateDTO("Esta é uma resposta de teste");
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateAnswerAndReturn201Created() throws Exception {
        when(answerService.createAnswer(eq(questionId), any(AnswerCreateDTO.class), eq(userId))).thenReturn(answerDTO);

        mockMvc.perform(post("/answers/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answerId").value(answerDTO.answerId().toString()))
                .andExpect(jsonPath("$.content").value(answerDTO.content()))
                .andExpect(jsonPath("$.accepted").value(answerDTO.accepted()))
                .andExpect(jsonPath("$.createdAt").value(answerDTO.createdAt().toString()))
                .andExpect(jsonPath("$.profile.profileId").value(answerDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.profile.userId").value(answerDTO.profile().userId().toString()))
                .andExpect(jsonPath("$.profile.name").value(answerDTO.profile().name()))
                .andExpect(jsonPath("$.profile.level").value(answerDTO.profile().level()))
                .andExpect(jsonPath("$.profile.professional").value(answerDTO.profile().professional()))
                .andExpect(jsonPath("$.profile.photoUrl").value(answerDTO.profile().photoUrl()))
                .andExpect(jsonPath("$.answerTotalLikes").value(answerDTO.answerTotalLikes()))
                .andExpect(jsonPath("$.liked").value(answerDTO.liked()));
    }

    @Test
    void shouldGetAllAnswersFromQuestionAndReturn200Ok() throws Exception {
        Page<AnswerDTO> page = new PageImpl<>(List.of(answerDTO));
        when(answerService.getAllAnswersFromQuestion(eq(questionId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/answers/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].answerId").value(answerDTO.answerId().toString()))
                .andExpect(jsonPath("$.content[0].content").value(answerDTO.content()))
                .andExpect(jsonPath("$.content[0].accepted").value(answerDTO.accepted()))
                .andExpect(jsonPath("$.content[0].createdAt").value(answerDTO.createdAt().toString()))
                .andExpect(jsonPath("$.content[0].profile.profileId").value(answerDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.content[0].profile.userId").value(answerDTO.profile().userId().toString()))
                .andExpect(jsonPath("$.content[0].profile.name").value(answerDTO.profile().name()))
                .andExpect(jsonPath("$.content[0].profile.level").value(answerDTO.profile().level()))
                .andExpect(jsonPath("$.content[0].profile.professional").value(answerDTO.profile().professional()))
                .andExpect(jsonPath("$.content[0].profile.photoUrl").value(answerDTO.profile().photoUrl()))
                .andExpect(jsonPath("$.content[0].answerTotalLikes").value(answerDTO.answerTotalLikes()))
                .andExpect(jsonPath("$.content[0].liked").value(answerDTO.liked()));
    }

    @Test
    void shouldAcceptAnswerAndReturn200Ok() throws Exception {
        doNothing().when(answerService).acceptAnswer(eq(answerId), eq(userId));

        mockMvc.perform(patch("/answers/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldToggleAnswerLikeAndReturn200Ok() throws Exception {
        doNothing().when(answerService).toggleAnswerLike(eq(answerId), eq(userId));

        mockMvc.perform(patch("/answers/{answerId}/like", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteAnswerAndReturn204NoContent() throws Exception {
        doNothing().when(answerService).deleteAnswerById(eq(answerId), eq(userId));

        mockMvc.perform(delete("/answers/{answerId}", answerId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateAnswerAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        AnswerCreateDTO invalidDTO = new AnswerCreateDTO("");

        mockMvc.perform(post("/answers/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateAnswerAndReturn404NotFoundWhenQuestionDoesNotExist() throws Exception {
        when(answerService.createAnswer(eq(questionId), any(AnswerCreateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Pergunta não encontrada"));

        mockMvc.perform(post("/answers/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerCreateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateAnswerAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(answerService.createAnswer(eq(questionId), any(AnswerCreateDTO.class), eq(userId)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não autorizado"));

        mockMvc.perform(post("/answers/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerCreateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotGetAllAnswersFromQuestionAndReturn404NotFoundWhenQuestionDoesNotExist() throws Exception {
        when(answerService.getAllAnswersFromQuestion(eq(questionId), any(Pageable.class)))
                .thenThrow(new ObjectNotFoundException("Pergunta não encontrada"));

        mockMvc.perform(get("/answers/{questionId}", questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAcceptAnswerAndReturn404NotFoundWhenAnswerDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Resposta não encontrada"))
                .when(answerService).acceptAnswer(eq(answerId), eq(userId));

        mockMvc.perform(patch("/answers/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAcceptAnswerAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono"))
                .when(answerService).acceptAnswer(eq(answerId), eq(userId));

        mockMvc.perform(patch("/answers/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAcceptAnswerAndReturn409ConflictWhenQuestionHaveAnswerAlreadyAccepted() throws Exception {
        doThrow(new QuestionHaveAnswerAlreadyAcceptedException("Pergunta já possui resposta aceita"))
                .when(answerService).acceptAnswer(eq(answerId), eq(userId));

        mockMvc.perform(patch("/answers/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotToggleAnswerLikeAndReturn404NotFoundWhenAnswerDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Resposta não encontrada"))
                .when(answerService).toggleAnswerLike(eq(answerId), eq(userId));

        mockMvc.perform(patch("/answers/{answerId}/like", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteAnswerAndReturn404NotFoundWhenAnswerDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Resposta não encontrada"))
                .when(answerService).deleteAnswerById(eq(answerId), eq(userId));

        mockMvc.perform(delete("/answers/{answerId}", answerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteAnswerAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono"))
                .when(answerService).deleteAnswerById(eq(answerId), eq(userId));

        mockMvc.perform(delete("/answers/{answerId}", answerId))
                .andExpect(status().isUnauthorized());
    }
}
