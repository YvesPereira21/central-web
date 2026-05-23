package io.centralweb.backend.service;

import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.QuestionCreateEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.QuestionMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import io.centralweb.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private TagService tagService;
    @Mock
    private ApplicationEventPublisher publisher;
    @InjectMocks
    private QuestionService questionService;
    private User userAdmin;
    private User userPerson1;
    private User userPerson2;
    private Profile profilePerson1;
    private Profile profilePerson2;
    private Question question1;
    private QuestionDTO questionDTO;
    private QuestionCreateDTO questionCreateDTO;
    private QuestionListDTO questionListDTO;
    private Tag tagJava;
    private Tag tagRust;
    private TagDTO tagJavaDTO;
    private TagDTO tagRustDTO;
    private ProfileSimpleDTO profileSimpleDTO;

    @BeforeEach
    public void setUp() {
        userAdmin = new User();
        ReflectionTestUtils.setField(userAdmin, "userId", UUID.randomUUID());
        userAdmin.setEmail("testcentraldev@gmail.com");
        userAdmin.setPassword("password");
        userAdmin.setRole(UserRole.ADMIN);

        userPerson1 = new User();
        ReflectionTestUtils.setField(userPerson1, "userId", UUID.randomUUID());
        userPerson1.setEmail("testcentralweb@gmail.com");
        userPerson1.setPassword("password");
        userPerson1.setRole(UserRole.PERSON);

        userPerson2 = new User();
        ReflectionTestUtils.setField(userPerson2, "userId", UUID.randomUUID());
        userPerson2.setEmail("testcentraljunior@gmail.com");
        userPerson2.setPassword("password");
        userPerson2.setRole(UserRole.PERSON);

        profilePerson1 = new Profile();
        ReflectionTestUtils.setField(profilePerson1, "profileId", UUID.randomUUID());
        profilePerson1.setName("Usuário Teste");
        profilePerson1.setBio("Sou um programador de testes");
        profilePerson1.setUser(userPerson1);

        profilePerson2 = new Profile();
        ReflectionTestUtils.setField(profilePerson2, "profileId", UUID.randomUUID());
        profilePerson2.setName("Usuário Teste 2");
        profilePerson2.setBio("Sou um programador de testes 2");
        profilePerson2.setUser(userPerson2);

        profileSimpleDTO = new ProfileSimpleDTO(
                profilePerson1.getProfileId(),
                profilePerson1.getName(),
                profilePerson1.getExpertise(),
                profilePerson1.getLevel(),
                profilePerson1.isProfessional()
        );

        tagJava = new Tag();
        tagJava.setTechnologyName("Java");
        tagJava.setColor("#ED8B00");

        tagRust = new Tag();
        tagRust.setTechnologyName("Rust");
        tagRust.setColor("#ED7C10");

        tagJavaDTO = new TagDTO("Java", "#ED8B00");
        tagRustDTO = new TagDTO("Rust", "#ED7C10");

        question1 = new Question();
        ReflectionTestUtils.setField(question1, "questionId", UUID.randomUUID());
        question1.setTitle("Diferenças entre Python x Java");
        question1.setContent("Content 1");
        question1.setPublished(true);
        question1.setSolutioned(true);
        question1.setCreatedAt(LocalDate.now());
        question1.setTags(List.of(tagJava, tagRust));
        question1.setProfile(profilePerson1);

        questionCreateDTO = new QuestionCreateDTO(
                "Diferenças entre Python x Java",
                "Content 1",
                List.of("Java", "Rust")
        );

        questionDTO = new QuestionDTO(
                question1.getQuestionId(),
                question1.getTitle(),
                question1.getContent(),
                question1.isPublished(),
                question1.isSolutioned(),
                question1.getCreatedAt(),
                profileSimpleDTO,
                List.of(tagJavaDTO, tagRustDTO),
                new ArrayList<>(),
                0L,
                false
        );

        questionListDTO = new QuestionListDTO(
                question1.getQuestionId(),
                question1.getTitle(),
                question1.getContent(),
                question1.isPublished(),
                question1.isSolutioned(),
                question1.getCreatedAt(),
                profileSimpleDTO,
                List.of(tagJavaDTO, tagRustDTO),
                0L,
                false
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateQuestion() {
        UUID userId = userPerson1.getUserId();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(tagService.convertTechnologyNamesToTags(questionCreateDTO.technologyNames()))
                .thenReturn(List.of(tagJava, tagRust));
        when(questionRepository.save(any(Question.class))).thenReturn(question1);
        when(questionMapper.toQuestionDTO(any(Question.class))).thenReturn(questionDTO);

        QuestionDTO result = questionService.createQuestion(questionCreateDTO, userId);

        assertNotNull(result);
        assertEquals("Diferenças entre Python x Java", result.title());
        assertEquals("Content 1", result.content());
        assertEquals(List.of(tagJavaDTO, tagRustDTO), result.tags());
        assertTrue(result.published());
        assertTrue(result.solutioned());
        assertEquals(LocalDate.now(), result.createdAt());
        assertEquals("Usuário Teste", result.profile().name());
        assertEquals("Iniciante", result.profile().level());
        assertFalse(result.profile().professional());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(tagService, times(1)).convertTechnologyNamesToTags(anyList());
        verify(questionRepository, times(1)).save(any(Question.class));
        verify(questionMapper, times(1)).toQuestionDTO(any(Question.class));

        ArgumentCaptor<QuestionCreateEvent> eventCaptor = ArgumentCaptor.forClass(QuestionCreateEvent.class);

        verify(publisher, times(1)).publishEvent(eventCaptor.capture());

        QuestionCreateEvent capturedEvent = eventCaptor.getValue();

        assertEquals(profilePerson1.getProfileId(), capturedEvent.profileId());
    }

    @Test
    void shouldReturnQuestion() {
        UUID questionId = question1.getQuestionId();

        when(questionRepository.findById(question1.getQuestionId())).thenReturn(Optional.of(question1));
        when(questionMapper.toQuestionDTO(any(Question.class))).thenReturn(questionDTO);

        QuestionDTO result = questionService.getQuestionById(questionId);

        assertNotNull(result);
        assertEquals("Diferenças entre Python x Java", result.title());
        assertEquals("Content 1", result.content());
        assertEquals(List.of(tagJavaDTO, tagRustDTO), result.tags());
        assertTrue(result.published());
        assertTrue(result.solutioned());
        assertEquals(LocalDate.now(), result.createdAt());
        assertEquals("Usuário Teste", result.profile().name());
        assertEquals("Iniciante", result.profile().level());
        assertFalse(result.profile().professional());

        verify(questionRepository, times(1)).findById(question1.getQuestionId());
        verify(questionMapper, times(1)).toQuestionDTO(any(Question.class));
    }

    @Test
    void shouldReturnPublishedQuestions() {
        List<Question> questions = List.of(question1);
        Page<Question> questionPage = new PageImpl<>(questions);

        when(questionRepository.findAllByPublishedIsTrue(any(Pageable.class))).thenReturn(questionPage);
        when(questionMapper.toQuestionListDTO(any(Question.class))).thenReturn(questionListDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionListDTO> result = questionService.getAllPublishedQuestions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(questionRepository, times(1)).findAllByPublishedIsTrue(any(Pageable.class));
        verify(questionMapper, times(1)).toQuestionListDTO(any(Question.class));
    }

    @Test
    void shouldReturnPublishedQuestionsByTitle() {
        String title = "Diferenças";
        List<Question> questions = List.of(question1);
        Page<Question> questionPage = new PageImpl<>(questions);

        when(questionRepository.findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(
                eq(title),
                any(Pageable.class)
        )).thenReturn(questionPage);
        when(questionMapper.toQuestionListDTO(any(Question.class))).thenReturn(questionListDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionListDTO> result = questionService.getAllPublishedQuestionsByTitle(title, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(questionRepository, times(1))
                .findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(eq(title), any(Pageable.class));
        verify(questionMapper, times(1)).toQuestionListDTO(any(Question.class));
    }

    @Test
    void shouldReturnPublishedQuestionsByTechnologyName() {
        String technologyName = "Rust";
        List<Question> questions = List.of(question1);
        Page<Question> questionPage = new PageImpl<>(questions);

        when(questionRepository.findAllByTags_TechnologyNameAndPublishedIsTrue(
                eq(technologyName),
                any(Pageable.class)
        ))
                .thenReturn(questionPage);
        when(questionMapper.toQuestionListDTO(any(Question.class))).thenReturn(questionListDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionListDTO> result = questionService.getAllPublishedQuestionsByTechnologyName(technologyName, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(questionRepository, times(1))
                .findAllByTags_TechnologyNameAndPublishedIsTrue(eq(technologyName), any(Pageable.class));
        verify(questionMapper, times(1)).toQuestionListDTO(any(Question.class));
    }

    @Test
    void shouldReturnPublishedQuestionWithAcceptedAnswer() {
        List<Question> questions = List.of(question1);
        Page<Question> questionPage = new PageImpl<>(questions);

        when(questionRepository.findPublishedQuestionsWithAcceptedAnswers(
                any(Pageable.class)
        ))
                .thenReturn(questionPage);
        when(questionMapper.toQuestionListDTO(any(Question.class))).thenReturn(questionListDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionListDTO> result = questionService.getAllPublishedQuestionWithAcceptedAnswer(pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(questionRepository, times(1))
                .findPublishedQuestionsWithAcceptedAnswers(any(Pageable.class));
        verify(questionMapper, times(1)).toQuestionListDTO(any(Question.class));
    }

    @Test
    void shouldUpdateQuestionWhenUserIsOwner() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = question1.getQuestionId();
        QuestionUpdateDTO updateDTO = new QuestionUpdateDTO(
                "Título Atualizado no Teste",
                "Conteúdo Modificado",
                List.of("Java")
        );
        QuestionDTO updatedQuestionDTO = new QuestionDTO(
                question1.getQuestionId(),
                "Título Atualizado no Teste",
                "Conteúdo Modificado",
                question1.isPublished(),
                question1.isSolutioned(),
                question1.getCreatedAt(),
                profileSimpleDTO,
                List.of(tagJavaDTO),
                new ArrayList<>(),
                0L,
                false
        );

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));
        when(tagService.convertTechnologyNamesToTags(updateDTO.technologyNames()))
                .thenReturn(List.of(tagJava, tagRust));
        when(questionRepository.save(any(Question.class))).thenReturn(question1);
        when(questionMapper.toQuestionDTO(any(Question.class))).thenReturn(updatedQuestionDTO);

        QuestionDTO result = questionService.updateQuestion(questionId, updateDTO, userId);

        assertNotNull(result);
        assertEquals("Título Atualizado no Teste", result.title());
        assertEquals("Conteúdo Modificado", result.content());
        assertEquals(List.of(tagJavaDTO), result.tags());

        verify(questionRepository, times(1)).findById(questionId);
        verify(tagService, times(1)).convertTechnologyNamesToTags(updateDTO.technologyNames());
        verify(questionMapper, times(1)).updateQuestionFromDTO(updateDTO, question1);
        verify(questionRepository, times(1)).save(question1);
        verify(questionMapper, times(1)).toQuestionDTO(question1);
    }

    @Test
    void shouldAddLikeWhenUserHasNotLiked() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = question1.getQuestionId();

        question1.setQuestionLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));

        assertDoesNotThrow(() -> questionService.toggleQuestionLike(questionId, userId));
        assertTrue(question1.getQuestionLikes().contains(profilePerson1));
        assertEquals(1, question1.getQuestionLikes().size());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, times(1)).save(question1);
    }

    @Test
    public void shouldRemoveLikeWhenUserHasLiked() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = question1.getQuestionId();

        List<Profile> likes = new ArrayList<>();
        likes.add(profilePerson1);
        question1.setQuestionLikes(likes);

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));

        assertDoesNotThrow(() -> questionService.toggleQuestionLike(questionId, userId));
        assertFalse(question1.getQuestionLikes().contains(profilePerson1));
        assertEquals(0, question1.getQuestionLikes().size());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, times(1)).save(question1);
    }

    @Test
    void shouldDeleteQuestionWhenUserIsOwner() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = question1.getQuestionId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));

        assertDoesNotThrow(() -> questionService.deleteQuestionById(questionId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, times(1)).delete(question1);
    }

    @Test
    void shouldDeleteQuestionWhenUserIsAdmin() {
        UUID userId = userAdmin.getUserId();
        UUID questionId = question1.getQuestionId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));

        assertDoesNotThrow(() -> questionService.deleteQuestionById(questionId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, times(1)).delete(question1);
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateQuestionAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.createQuestion(questionCreateDTO, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(questionRepository, never()).save(any(Question.class));
        verify(questionMapper, never()).toQuestionDTO(any(Question.class));
    }

    @Test
    void shouldNotReturnQuestionAndThrowExceptionWhenQuestionDoesNotExist() {
        UUID questionId = UUID.randomUUID();

        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.getQuestionById(questionId)
        );

        verify(questionRepository, times(1)).findById(questionId);
        verify(questionMapper, never()).toQuestionDTO(any(Question.class));
    }

    @Test
    void shouldNotUpdateQuestionAndThrowExceptionWhenQuestionDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = UUID.randomUUID();
        QuestionUpdateDTO updateDTO = new QuestionUpdateDTO(
                "Título", "Conteúdo", List.of("Java")
        );

        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.updateQuestion(questionId, updateDTO, userId)
        );

        verify(questionRepository, times(1)).findById(questionId);
        verify(tagService, never()).convertTechnologyNamesToTags(anyList());
        verify(questionMapper, never()).updateQuestionFromDTO(any(), any());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldNotUpdateQuestionAndThrowExceptionWhenUserIsNotOwner() {
        UUID userId = userPerson2.getUserId();
        UUID questionId = question1.getQuestionId();
        QuestionUpdateDTO updateDTO = new QuestionUpdateDTO(
                "Título", "Conteúdo", List.of("Java")
        );

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> questionService.updateQuestion(questionId, updateDTO, userId)
        );

        verify(questionRepository, times(1)).findById(questionId);
        verify(tagService, never()).convertTechnologyNamesToTags(anyList());
        verify(questionMapper, never()).updateQuestionFromDTO(any(), any());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldNotUpdateQuestionAndThrowExceptionWhenTechnologyNameDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = question1.getQuestionId();
        QuestionUpdateDTO updateDTO = new QuestionUpdateDTO(
                "Título", "Conteúdo", List.of("Go")
        );

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));
        when(tagService.convertTechnologyNamesToTags(anyList()))
                .thenThrow(new ObjectNotFoundException("Tag Go não encontrada"));

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.updateQuestion(questionId, updateDTO, userId)
        );

        verify(questionRepository, times(1)).findById(questionId);
        verify(questionMapper, times(1)).updateQuestionFromDTO(updateDTO, question1);
        verify(tagService, times(1)).convertTechnologyNamesToTags(anyList());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldNotAddLikeAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID questionId = question1.getQuestionId();

        question1.setQuestionLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.toggleQuestionLike(questionId, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(questionRepository, never()).findById(questionId);
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldNotAddLikeAndThrowExceptionWhenQuestionDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = UUID.randomUUID();

        question1.setQuestionLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.toggleQuestionLike(questionId, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldNotDeleteQuestionAndThrowExceptionWhenQuestionDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID questionId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> questionService.deleteQuestionById(questionId, userId)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, never()).delete(any(Question.class));
    }

    @Test
    void shouldNotDeleteQuestionAndThrowExceptionWhenUserIsNotOwnerAndIsNotAdmin() {
        UUID userId = userPerson2.getUserId();
        UUID questionId = question1.getQuestionId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson2));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> questionService.deleteQuestionById(questionId, userId)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, never()).delete(any(Question.class));
    }
}