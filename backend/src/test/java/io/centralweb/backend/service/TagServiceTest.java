package io.centralweb.backend.service;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.mapper.TagMapper;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TagMapper tagMapper;
    @InjectMocks
    private TagService tagService;
    private Tag tagJava;
    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        tagJava = new Tag();
        ReflectionTestUtils.setField(tagJava, "tagId", UUID.randomUUID());
        tagJava.setTechnologyName("Java");
        tagJava.setColor("#ED8B00");

        tagDTO = new TagDTO("Java", "#ED8B00");
    }

    @Test
    void shouldCreateTag() {
        when(tagRepository.existsByTechnologyName("Java")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(tagJava);
        when(tagMapper.toDTO(any(Tag.class))).thenReturn(tagDTO);

        TagDTO result = tagService.createTag(tagDTO);

        assertNotNull(result);
        assertEquals("Java", result.technologyName());
        assertEquals("#ED8B00", result.color());

        verify(tagRepository, times(1)).save(any(Tag.class));
        verify(tagMapper, times(1)).toDTO(any(Tag.class));
    }

    @Test
    void shouldReturnTags(){
        Tag tagPython = new Tag();
        tagPython.setTechnologyName("Python");
        tagPython.setColor("#6DB33A");
        ReflectionTestUtils.setField(tagPython, "tagId", UUID.randomUUID());
        TagDTO tagPythonDTO = new TagDTO("Python", "#6DB33A");

        List<Tag> tags = List.of(tagPython, tagJava);
        Page<Tag> tagPage = new PageImpl<>(tags);

        when(tagRepository.findAll(any(Pageable.class))).thenReturn(tagPage);
        when(tagMapper.toDTO(tagJava)).thenReturn(tagDTO);
        when(tagMapper.toDTO(tagPython)).thenReturn(tagPythonDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<TagDTO> result = tagService.getAllTags(pageable);

        assertNotNull(result);
        assertEquals(2, result.getNumberOfElements());
        assertEquals("Python", result.getContent().get(0).technologyName());
        assertEquals("Java", result.getContent().get(1).technologyName());

        verify(tagRepository, times(1)).findAll(any(Pageable.class));
        verify(tagMapper, times(2)).toDTO(any(Tag.class));
    }

    @Test
    void shouldUpdateTag(){
        UUID tagId = tagJava.getTagId();
        TagUpdateDTO tagSwiftDTO = new TagUpdateDTO(
                "Swift",
                "#6DB33A"
        );
        TagDTO tagSwiftResponseDTO = new TagDTO("Swift", "#6DB33A");

        when(tagRepository.findById(tagId)).thenReturn(Optional.ofNullable(tagJava));
        when(tagRepository.save(any(Tag.class))).thenReturn(tagJava);
        when(tagMapper.toDTO(any(Tag.class))).thenReturn(tagSwiftResponseDTO);

        TagDTO result = tagService.updateTag(tagId, tagSwiftDTO);

        assertNotNull(result);
        assertEquals("Swift", result.technologyName());
        assertEquals("#6DB33A", result.color());

        verify(tagRepository, times(1)).findById(tagId);
        verify(tagMapper, times(1)).updateTagFromDTO(tagSwiftDTO, tagJava);
        verify(tagRepository, times(1)).save(tagJava);
    }

    @Test
    void shouldDeleteTag(){
        String technologyName = tagJava.getTechnologyName();

        when(tagRepository.findByTechnologyName(technologyName)).thenReturn(Optional.of(tagJava));

        assertDoesNotThrow(() -> tagService.deleteTagByTechnologyName(technologyName));

        verify(tagRepository, times(1)).findByTechnologyName(technologyName);
        verify(tagRepository, times(1)).delete(tagJava);
    }

    @Test
    void shouldConvertTags(){
        List<String> technologyNames = List.of("Java", "Rust");

        Tag tagJava = new Tag();
        tagJava.setTechnologyName("Java");

        Tag tagRust = new Tag();
        tagRust.setTechnologyName("Rust");

        when(tagRepository.findByTechnologyName("Java")).thenReturn(Optional.of(tagJava));
        when(tagRepository.findByTechnologyName("Rust")).thenReturn(Optional.of(tagRust));

        List<Tag> result = tagService.convertTechnologyNamesToTags(technologyNames);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getTechnologyName());
        assertEquals("Rust", result.get(1).getTechnologyName());

        verify(tagRepository, times(2)).findByTechnologyName(anyString());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateAndThrowExceptionWhenTechnologyNameAlreadyExists() {
        String javaTechnologyName = tagJava.getTechnologyName();

        when(tagRepository.existsByTechnologyName(javaTechnologyName)).thenReturn(true);

        assertThrows(
                ObjectAlreadyExistsException.class,
                () -> tagService.createTag(tagDTO)
        );

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void shouldNotUpdateAndThrowExceptionWhenTechnologyNameDoesNotExist() {
        UUID tagId = UUID.randomUUID();
        TagUpdateDTO tagSwiftDTO = new TagUpdateDTO(
                "Swift",
                "#6DB33A"
        );

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> tagService.updateTag(tagId, tagSwiftDTO)
        );

        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagMapper, never()).updateTagFromDTO(any(), any());
        verify(tagMapper, never()).toDTO(any(Tag.class));
    }

    @Test
    void shouldNotUpdateAndThrowExceptionWhenTechnologyNameAlreadyExists() {
        UUID tagId = tagJava.getTagId();
        TagUpdateDTO tagUpdateDTO = new TagUpdateDTO(
                "Swift",
                "#6DB33A"
        );

        when(tagRepository.findById(tagId)).thenReturn(Optional.ofNullable(tagJava));
        when(tagRepository.existsByTechnologyName("Swift")).thenReturn(true);

        assertThrows(
                ObjectAlreadyExistsException.class,
                () -> tagService.updateTag(tagId, tagUpdateDTO)
        );

        verify(tagRepository, times(1)).findById(tagId);
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagMapper, never()).updateTagFromDTO(tagUpdateDTO, tagJava);
        verify(tagMapper, never()).toDTO(any(Tag.class));
    }

    @Test
    void shouldNotDeleteAndThrowExceptionWhenTechnologyNameDoesNotExist() {
        String technologyName = "Python";

        when(tagRepository.findByTechnologyName(technologyName)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> tagService.deleteTagByTechnologyName(technologyName)
        );

        verify(tagRepository, times(1)).findByTechnologyName(technologyName);
        verify(tagRepository, never()).delete(any(Tag.class));
    }

    @Test
    void shouldNotConvertTagsAndThrowExceptionWhenTechnologyNameDoesNotExist(){
        List<String> technologyNames = List.of("Java", "Rust", "Ruby");

        Tag tagJava = new Tag();
        tagJava.setTechnologyName("Java");

        Tag tagRuby = new Tag();
        tagRuby.setTechnologyName("Ruby");

        when(tagRepository.findByTechnologyName("Java")).thenReturn(Optional.of(tagJava));
        when(tagRepository.findByTechnologyName("Rust")).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> tagService.convertTechnologyNamesToTags(technologyNames)
        );

        verify(tagRepository, times(2)).findByTechnologyName(anyString());
    }
}