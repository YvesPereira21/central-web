package io.centralweb.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.OAuth2SuccessHandler;
import io.centralweb.backend.security.SecurityFilter;
import io.centralweb.backend.security.TokenService;
import io.centralweb.backend.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private TagService tagService;

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

    private UUID tagId;
    private TagDTO tagDTO;
    private TagUpdateDTO tagUpdateDTO;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();
        tagDTO = new TagDTO("Java", "#FF0000");
        tagUpdateDTO = new TagUpdateDTO("Java 21", "#00FF00");
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateTagAndReturn201Created() throws Exception {
        when(tagService.createTag(any(TagDTO.class))).thenReturn(tagDTO);

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technologyName").value(tagDTO.technologyName()))
                .andExpect(jsonPath("$.color").value(tagDTO.color()));
    }

    @Test
    void shouldGetAllTagsAndReturn200Ok() throws Exception {
        Page<TagDTO> page = new PageImpl<>(List.of(tagDTO));
        when(tagService.getAllTags(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].technologyName").value(tagDTO.technologyName()))
                .andExpect(jsonPath("$.content[0].color").value(tagDTO.color()));
    }

    @Test
    void shouldUpdateTagAndReturn200Ok() throws Exception {
        when(tagService.updateTag(eq(tagId), any(TagUpdateDTO.class))).thenReturn(tagDTO);

        mockMvc.perform(put("/tags/{tagId}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technologyName").value(tagDTO.technologyName()))
                .andExpect(jsonPath("$.color").value(tagDTO.color()));
    }

    @Test
    void shouldDeleteTagAndReturn204NoContent() throws Exception {
        doNothing().when(tagService).deleteTagByTechnologyName(eq("Java"));

        mockMvc.perform(delete("/tags/{technologyName}", "Java"))
                .andExpect(status().isNoContent());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateTagAndReturn400BadRequestWhenDataIsInvalid() throws Exception {
        TagDTO invalidDTO = new TagDTO("", "");

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateTagAndReturn409ConflictWhenTagAlreadyExists() throws Exception {
        when(tagService.createTag(any(TagDTO.class)))
                .thenThrow(new ObjectAlreadyExistsException("Tag já existe"));

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotUpdateTagAndReturn404NotFoundWhenTagDoesNotExist() throws Exception {
        when(tagService.updateTag(eq(tagId), any(TagUpdateDTO.class)))
                .thenThrow(new ObjectNotFoundException("Tag não encontrada"));

        mockMvc.perform(put("/tags/{tagId}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteTagAndReturn404NotFoundWhenTagDoesNotExist() throws Exception {
        doThrow(new ObjectNotFoundException("Tag não encontrada"))
                .when(tagService).deleteTagByTechnologyName(eq("Java"));

        mockMvc.perform(delete("/tags/{technologyName}", "Java"))
                .andExpect(status().isNotFound());
    }
}
