package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.ExperienceLevel;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.QualificationService;
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

@WebMvcTest(QualificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QualificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private QualificationService qualificationService;

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
    private UUID qualificationId;

    private QualificationDTO qualificationDTO;
    private QualificationCreateDTO qualificationCreateDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        qualificationId = UUID.randomUUID();

        User user = new User(userId, "password", "user@test.com");
        user.setRole(UserRole.PERSON);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        qualificationDTO = new QualificationDTO(
                qualificationId,
                "Desenvolvedor Backend",
                ExperienceLevel.SENIOR,
                "Tech Corp",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 12, 31),
                false,
                profileId,
                "Usuário Teste"
        );

        qualificationCreateDTO = new QualificationCreateDTO(
                "Desenvolvedor Backend",
                ExperienceLevel.SENIOR,
                "Tech Corp",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateQualificationAndReturn201Created() throws Exception {
        when(qualificationService.createQualification(any(QualificationCreateDTO.class), eq(userId))).thenReturn(qualificationDTO);

        mockMvc.perform(post("/qualifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualificationCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value(qualificationDTO.jobTitle()))
                .andExpect(jsonPath("$.experienceLevel").value(qualificationDTO.experienceLevel().getExperienceLevel()))
                .andExpect(jsonPath("$.institution").value(qualificationDTO.institution()))
                .andExpect(jsonPath("$.startDate").value(qualificationDTO.startDate().toString()))
                .andExpect(jsonPath("$.endDate").value(qualificationDTO.endDate().toString()));
    }

    @Test
    void shouldGetVerifiedQualificationsAndReturn200Ok() throws Exception {
        Page<QualificationDTO> page = new PageImpl<>(List.of(qualificationDTO));
        when(qualificationService.getAllQualificationsVerified(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/qualifications/verified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobTitle").value(qualificationDTO.jobTitle()))
                .andExpect(jsonPath("$.content[0].experienceLevel").value(qualificationDTO.experienceLevel().getExperienceLevel()))
                .andExpect(jsonPath("$.content[0].institution").value(qualificationDTO.institution()))
                .andExpect(jsonPath("$.content[0].startDate").value(qualificationDTO.startDate().toString()))
                .andExpect(jsonPath("$.content[0].endDate").value(qualificationDTO.endDate().toString()));
    }

    @Test
    void shouldGetNotVerifiedQualificationsAndReturn200Ok() throws Exception {
        Page<QualificationDTO> page = new PageImpl<>(List.of(qualificationDTO));
        when(qualificationService.getAllNotVerifiedQualifications(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/qualifications/not-verified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobTitle").value(qualificationDTO.jobTitle()));
    }

    @Test
    void shouldGetProfileVerifiedQualificationsAndReturn200Ok() throws Exception {
        Page<QualificationDTO> page = new PageImpl<>(List.of(qualificationDTO));
        when(qualificationService.getAllProfileVerifiedQualifications(eq(profileId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/qualifications/{profileId}/verified", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobTitle").value(qualificationDTO.jobTitle()));
    }

    @Test
    void shouldGetProfileNotVerifiedQualificationsAndReturn200Ok() throws Exception {
        Page<QualificationDTO> page = new PageImpl<>(List.of(qualificationDTO));
        when(qualificationService.getAllProfileNotVerifiedQualifications(eq(profileId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/qualifications/{profileId}/not-verified", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobTitle").value(qualificationDTO.jobTitle()));
    }

    @Test
    void shouldVerifyQualificationAndReturn200Ok() throws Exception {
        doNothing().when(qualificationService).markAsVerified(eq(qualificationId));

        mockMvc.perform(patch("/qualifications/{qualificationId}", qualificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteQualificationAndReturn204NoContent() throws Exception {
        doNothing().when(qualificationService).deleteQualificationById(eq(qualificationId), eq(userId));

        mockMvc.perform(delete("/qualifications/{qualificationId}", qualificationId))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateQualificationAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        QualificationCreateDTO invalidDTO = new QualificationCreateDTO("", null, "", null, null);

        mockMvc.perform(post("/qualifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateQualificationAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(qualificationService.createQualification(any(QualificationCreateDTO.class), eq(userId)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(post("/qualifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualificationCreateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetProfileVerifiedQualificationsAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(qualificationService.getAllProfileVerifiedQualifications(eq(profileId), any(Pageable.class)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/qualifications/{profileId}/verified", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetProfileNotVerifiedQualificationsAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        when(qualificationService.getAllProfileNotVerifiedQualifications(eq(profileId), any(Pageable.class)))
                .thenThrow(new ObjectNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/qualifications/{profileId}/not-verified", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotVerifyQualificationAndReturn404NotFoundWhenQualificationDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Qualificação não encontrada"))
                .when(qualificationService).markAsVerified(eq(qualificationId));

        mockMvc.perform(patch("/qualifications/{qualificationId}", qualificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteQualificationAndReturn404NotFoundWhenQualificationDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Qualificação não encontrada"))
                .when(qualificationService).deleteQualificationById(eq(qualificationId), eq(userId));

        mockMvc.perform(delete("/qualifications/{qualificationId}", qualificationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteQualificationAndReturn401UnauthorizedWhenUserIsNotOwner() throws Exception {
        doThrow(new ProfileIsNotTheOwnerException("Usuário não é o dono"))
                .when(qualificationService).deleteQualificationById(eq(qualificationId), eq(userId));

        mockMvc.perform(delete("/qualifications/{qualificationId}", qualificationId))
                .andExpect(status().isUnauthorized());
    }
}
