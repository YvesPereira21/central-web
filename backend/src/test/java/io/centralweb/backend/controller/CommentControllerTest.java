package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.CommentService;
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

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private CommentService commentService;

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
    private UUID answerId;
    private UUID commentId;
    private UUID profileId;

    private CommentDTO commentDTO;
    private CommentCreateDTO commentCreateDTO;
    private CommentUpdateDTO commentUpdateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        answerId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ProfileSimpleDTO profileSimpleDTO = new ProfileSimpleDTO(
                profileId,
                userId,
                "Commenter Name",
                "Intermediate",
                false,
                "http://example.com/avatar.jpg"
        );

        commentDTO = new CommentDTO(
                commentId,
                "Este é um comentário de teste.",
                LocalDate.of(2026, 8, 13),
                profileSimpleDTO
        );

        commentCreateDTO = new CommentCreateDTO("Este é um comentário de teste.");
        commentUpdateDTO = new CommentUpdateDTO("Comentário atualizado.");
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateCommentAndReturn201Created() throws Exception {
        when(commentService.createComment(eq(answerId), any(CommentCreateDTO.class), eq(userId))).thenReturn(commentDTO);

        mockMvc.perform(post("/comments/answer/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(commentDTO.commentId().toString()))
                .andExpect(jsonPath("$.content").value(commentDTO.content()))
                .andExpect(jsonPath("$.createdAt").value(commentDTO.createdAt().toString()))
                .andExpect(jsonPath("$.profile.profileId").value(commentDTO.profile().profileId().toString()))
                .andExpect(jsonPath("$.profile.userId").value(commentDTO.profile().userId().toString()))
                .andExpect(jsonPath("$.profile.name").value(commentDTO.profile().name()))
                .andExpect(jsonPath("$.profile.level").value(commentDTO.profile().level()))
                .andExpect(jsonPath("$.profile.professional").value(commentDTO.profile().professional()))
                .andExpect(jsonPath("$.profile.photoUrl").value(commentDTO.profile().photoUrl()));
    }

    @Test
    void shouldGetCommentsByAnswerAndReturn200Ok() throws Exception {
        Page<CommentDTO> page = new PageImpl<>(List.of(commentDTO));
        when(commentService.getAllCommentsFromAnswer(eq(answerId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/comments/answer/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].commentId").value(commentDTO.commentId().toString()))
                .andExpect(jsonPath("$.content[0].content").value(commentDTO.content()))
                .andExpect(jsonPath("$.content[0].createdAt").value(commentDTO.createdAt().toString()))
                .andExpect(jsonPath("$.content[0].profile.profileId").value(commentDTO.profile().profileId().toString()));
    }

    @Test
    void shouldUpdateCommentAndReturn200Ok() throws Exception {
        when(commentService.updateComment(eq(commentId), any(CommentUpdateDTO.class), eq(userId))).thenReturn(commentDTO);

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(commentDTO.commentId().toString()))
                .andExpect(jsonPath("$.content").value(commentDTO.content()));
    }

    @Test
    void shouldDeleteCommentAndReturn204NoContent() throws Exception {
        doNothing().when(commentService).deleteCommentById(eq(commentId), eq(userId));

        mockMvc.perform(delete("/comments/{commentId}", commentId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateCommentAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        CommentCreateDTO invalidDTO = new CommentCreateDTO("");

        mockMvc.perform(post("/comments/answer/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateCommentAndReturn404NotFoundWhenAnswerDoesNotExist() throws Exception {
        when(commentService.createComment(eq(answerId), any(CommentCreateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Resposta não encontrada"));

        mockMvc.perform(post("/comments/answer/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetCommentsByAnswerAndReturn404NotFoundWhenAnswerDoesNotExist() throws Exception {
        when(commentService.getAllCommentsFromAnswer(eq(answerId), any(Pageable.class)))
                .thenThrow(new ObjectNotFoundException("Resposta não encontrada"));

        mockMvc.perform(get("/comments/answer/{answerId}", answerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateCommentAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        CommentUpdateDTO invalidDTO = new CommentUpdateDTO("");

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateCommentAndReturn404NotFoundWhenCommentDoesNotExist() throws Exception {
        when(commentService.updateComment(eq(commentId), any(CommentUpdateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Comentário não encontrado"));

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateCommentAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(commentService.updateComment(eq(commentId), any(CommentUpdateDTO.class), eq(userId)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono do comentário"));

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteCommentAndReturn404NotFoundWhenCommentDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Comentário não encontrado"))
                .when(commentService).deleteCommentById(eq(commentId), eq(userId));

        mockMvc.perform(delete("/comments/{commentId}", commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteCommentAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono do comentário"))
                .when(commentService).deleteCommentById(eq(commentId), eq(userId));

        mockMvc.perform(delete("/comments/{commentId}", commentId))
                .andExpect(status().isUnauthorized());
    }
}
