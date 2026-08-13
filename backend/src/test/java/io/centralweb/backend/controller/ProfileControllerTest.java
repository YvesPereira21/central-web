package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.dto.user.UserDTO;
import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.ProfileType;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private ProfileService profileService;

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
    private UUID profileId;

    private ProfileDTO profileDTO;
    private ProfileCreateDTO profileCreateDTO;
    private ProfileUpdateDTO profileUpdateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        profileDTO = new ProfileDTO(
                profileId,
                userId,
                "Alice Smith",
                "Desenvolvedora Java Backend",
                "Senior",
                150L,
                true,
                5L,
                10L,
                "http://example.com/alice.jpg",
                Collections.emptyList()
        );

        UserDTO userDTO = new UserDTO("alice@test.com", "Password123!");
        profileCreateDTO = new ProfileCreateDTO("Alice Smith", "Desenvolvedora Java Backend", ProfileType.PROFESSIONAL, userDTO);
        profileUpdateDTO = new ProfileUpdateDTO("Alice Smith Updated", "Nova Bio");
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateProfileAndReturn201Created() throws Exception {
        when(profileService.createProfile(any(ProfileCreateDTO.class))).thenReturn(profileDTO);

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").value(profileDTO.profileId().toString()))
                .andExpect(jsonPath("$.userId").value(profileDTO.userId().toString()))
                .andExpect(jsonPath("$.name").value(profileDTO.name()))
                .andExpect(jsonPath("$.bio").value(profileDTO.bio()))
                .andExpect(jsonPath("$.level").value(profileDTO.level()))
                .andExpect(jsonPath("$.reputationScore").value(profileDTO.reputationScore()))
                .andExpect(jsonPath("$.professional").value(profileDTO.professional()))
                .andExpect(jsonPath("$.articlesCreatedByProfile").value(profileDTO.articlesCreatedByProfile()))
                .andExpect(jsonPath("$.answersAccepted").value(profileDTO.answersAccepted()))
                .andExpect(jsonPath("$.photoUrl").value(profileDTO.photoUrl()));
    }

    @Test
    void shouldGetMyProfileAndReturn200Ok() throws Exception {
        when(profileService.getProfileByUserId(eq(userId))).thenReturn(profileDTO);

        mockMvc.perform(get("/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(profileDTO.profileId().toString()))
                .andExpect(jsonPath("$.userId").value(profileDTO.userId().toString()))
                .andExpect(jsonPath("$.name").value(profileDTO.name()))
                .andExpect(jsonPath("$.bio").value(profileDTO.bio()))
                .andExpect(jsonPath("$.level").value(profileDTO.level()))
                .andExpect(jsonPath("$.reputationScore").value(profileDTO.reputationScore()))
                .andExpect(jsonPath("$.professional").value(profileDTO.professional()))
                .andExpect(jsonPath("$.articlesCreatedByProfile").value(profileDTO.articlesCreatedByProfile()))
                .andExpect(jsonPath("$.answersAccepted").value(profileDTO.answersAccepted()))
                .andExpect(jsonPath("$.photoUrl").value(profileDTO.photoUrl()));
    }

    @Test
    void shouldGetProfileAndReturn200Ok() throws Exception {
        when(profileService.getProfileById(eq(profileId))).thenReturn(profileDTO);

        mockMvc.perform(get("/profiles/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(profileDTO.profileId().toString()))
                .andExpect(jsonPath("$.name").value(profileDTO.name()));
    }

    @Test
    void shouldUpdateProfileAndReturn200Ok() throws Exception {
        when(profileService.updateProfile(eq(profileId), any(ProfileUpdateDTO.class), eq(userId))).thenReturn(profileDTO);

        mockMvc.perform(put("/profiles/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(profileDTO.profileId().toString()))
                .andExpect(jsonPath("$.name").value(profileDTO.name()));
    }

    @Test
    void shouldDeleteProfileAndReturn204NoContent() throws Exception {
        doNothing().when(profileService).deleteProfileById(eq(profileId), eq(userId));

        mockMvc.perform(delete("/profiles/{profileId}", profileId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateProfileAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        ProfileCreateDTO invalidDTO = new ProfileCreateDTO("", "", null, new UserDTO("invalid", ""));

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateProfileAndReturn409ConflictWhenUserAlreadyExists() throws Exception {
        when(profileService.createProfile(any(ProfileCreateDTO.class)))
                .thenThrow(new ObjectAlreadyExistsException("Usuário já cadastrado"));

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileCreateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotGetMyProfileAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(profileService.getProfileByUserId(eq(userId)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetProfileAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(profileService.getProfileById(eq(profileId)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/profiles/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateProfileAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(profileService.updateProfile(eq(profileId), any(ProfileUpdateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(put("/profiles/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateProfileAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        when(profileService.updateProfile(eq(profileId), any(ProfileUpdateDTO.class), eq(userId)))
                .thenThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono do perfil"));

        mockMvc.perform(put("/profiles/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileUpdateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteProfileAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Perfil não encontrado"))
                .when(profileService).deleteProfileById(eq(profileId), eq(userId));

        mockMvc.perform(delete("/profiles/{profileId}", profileId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteProfileAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono do perfil"))
                .when(profileService).deleteProfileById(eq(profileId), eq(userId));

        mockMvc.perform(delete("/profiles/{profileId}", profileId))
                .andExpect(status().isUnauthorized());
    }
}
