package io.centralweb.backend.controller;

import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhotoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PhotoService photoService;

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

    private UUID profileId;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldUploadAvatarAndReturn200Ok() throws Exception {
        doNothing().when(photoService).uploadAvatar(eq(profileId), any());

        mockMvc.perform(multipart("/photos/{profileId}/avatar", profileId)
                        .file(file))
                .andExpect(status().isOk());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotUploadAvatarAndReturn500InternalServerErrorWhenIOExceptionOccurs() throws Exception {
        doThrow(new IOException("Erro ao salvar arquivo"))
                .when(photoService).uploadAvatar(eq(profileId), any());

        mockMvc.perform(multipart("/photos/{profileId}/avatar", profileId)
                        .file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldNotUploadAvatarAndReturn404NotFoundWhenProfileDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Perfil não encontrado"))
                .when(photoService).uploadAvatar(eq(profileId), any());

        mockMvc.perform(multipart("/photos/{profileId}/avatar", profileId)
                        .file(file))
                .andExpect(status().isNotFound());
    }
}
