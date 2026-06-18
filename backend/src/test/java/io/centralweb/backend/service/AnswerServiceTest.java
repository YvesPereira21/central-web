package io.centralweb.backend.service;

import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.AnswerMapper;
import io.centralweb.backend.model.*;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import io.centralweb.backend.repository.UserRepository;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private AnswerMapper answerMapper;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @InjectMocks
    private AnswerService answerService;
    private User userAdmin;
    private User userPerson1;
    private User userPerson2;
    private Profile profilePerson1;
    private Profile profilePerson2;
    private Tag tagJava;
    private Tag tagRust;
    private Question question1;
    private Answer answer1;
    private AnswerDTO answerDTO;
    private AnswerCreateDTO answerCreateDTO;
    private ProfileSimpleDTO profileSimpleDTO;

    @BeforeEach
    void setUp() {
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

        tagJava = new Tag();
        tagJava.setTechnologyName("Java");
        tagJava.setColor("#ED8B00");

        tagRust = new Tag();
        tagRust.setTechnologyName("Rust");
        tagRust.setColor("#ED7C10");

        question1 = new Question();
        ReflectionTestUtils.setField(question1, "questionId", UUID.randomUUID());
        question1.setTitle("Diferenças entre Python x Java");
        question1.setContent("Content 1");
        question1.setPublished(true);
        question1.setSolutioned(true);
        question1.setCreatedAt(LocalDate.now());
        question1.setTags(List.of(tagJava, tagRust));
        question1.setProfile(profilePerson1);

        answer1 = new Answer();
        ReflectionTestUtils.setField(answer1, "answerId", UUID.randomUUID());
        answer1.setContent("A maior diferença é que Java é compilado, usado em grandes corporações");
        answer1.setAccepted(false);
        answer1.setCreatedAt(LocalDate.now());
        answer1.setQuestion(question1);
        answer1.setProfile(profilePerson2);

        profileSimpleDTO = new ProfileSimpleDTO(
                profilePerson1.getProfileId(),
                profilePerson1.getUser().getUserId(),
                profilePerson1.getName(),
                profilePerson1.getLevel(),
                profilePerson1.isProfessional(),
                null
        );

        answerCreateDTO = new AnswerCreateDTO(
                "A maior diferença é que Java é compilado, usado em grandes corporações"
        );

        answerDTO = new AnswerDTO(
                answer1.getAnswerId(),
                answer1.getContent(),
                answer1.isAccepted(),
                answer1.getCreatedAt(),
                profileSimpleDTO,
                0L,
                false
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateAnswer() {
        UUID questionId = question1.getQuestionId();
        UUID userId = userPerson1.getUserId();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(answerRepository.save(any(Answer.class))).thenReturn(answer1);
        when(answerMapper.toDTO(any(Answer.class))).thenReturn(answerDTO);

        AnswerDTO result = answerService.createAnswer(questionId, answerCreateDTO, userId);

        assertNotNull(result);
        assertEquals("A maior diferença é que Java é compilado, usado em grandes corporações", result.content());
        assertFalse(result.accepted());
        assertEquals(LocalDate.now(), result.createdAt());
        assertEquals(0, result.answerTotalLikes());

        verify(questionRepository, times(1)).findById(questionId);
        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(answerRepository, times(1)).save(any(Answer.class));
        verify(answerMapper, times(1)).toDTO(any(Answer.class));
    }

    @Test
    void shouldReturnAnswersFromQuestion() {
        UUID questionId = question1.getQuestionId();
        List<Answer> answers = List.of(answer1);
        Page<Answer> answerPage = new PageImpl<>(answers);

        when(answerRepository.findAllByQuestionIdAndQuestionPublished(
                eq(questionId),
                any(Pageable.class)
        )).thenReturn(answerPage);
        when(answerMapper.toDTO(any(Answer.class))).thenReturn(answerDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AnswerDTO> result = answerService.getAllAnswersFromQuestion(questionId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(answerRepository, times(1)).findAllByQuestionIdAndQuestionPublished(
                eq(questionId),
                any(Pageable.class)
        );
        verify(answerMapper, times(1)).toDTO(any(Answer.class));
    }

    @Test
    void shouldAcceptAnswerWhenUserIsOwnerQuestion() {
        UUID answerId = answer1.getAnswerId();
        UUID userId = userPerson1.getUserId();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertDoesNotThrow(() -> answerService.acceptAnswer(answerId, userId));
        assertTrue(answer1.isAccepted());

        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, times(1)).save(answer1);
    }

    @Test
    void shouldAddLikeWhenUserHasNotLiked() {
        UUID answerId = answer1.getAnswerId();
        UUID userId = userPerson1.getUserId();

        answer1.setAnswerLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertDoesNotThrow(() -> answerService.toggleAnswerLike(answerId, userId));
        assertTrue(answer1.getAnswerLikes().contains(profilePerson1));
        assertEquals(1, answer1.getAnswerLikes().size());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, times(1)).save(answer1);
    }

    @Test
    void shouldRemoveLikeWhenUserHasLiked() {
        UUID answerId = answer1.getAnswerId();
        UUID userId = userPerson1.getUserId();

        List<Profile> likes  = new ArrayList<>();
        likes.add(profilePerson1);
        answer1.setAnswerLikes(likes);

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertDoesNotThrow(() -> answerService.toggleAnswerLike(answerId, userId));
        assertFalse(answer1.getAnswerLikes().contains(profilePerson1));
        assertEquals(0, answer1.getAnswerLikes().size());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, times(1)).save(answer1);
    }

    @Test
    void shouldDeleteAnswerWhenUserIsOwner() {
        UUID answerId = answer1.getAnswerId();
        UUID userId = userPerson2.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson2));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertDoesNotThrow(() -> answerService.deleteAnswerById(answerId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, times(1)).delete(answer1);
    }

    @Test
    void shouldDeleteAnswerWhenUserIsAdmin() {
        UUID answerId = answer1.getAnswerId();
        UUID userId = userAdmin.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertDoesNotThrow(() -> answerService.deleteAnswerById(answerId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, times(1)).delete(answer1);
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateAnswerAndThrowExceptionWhenQuestionDoesNotExist() {
        UUID questionId = question1.getQuestionId();
        UUID userId = userPerson1.getUserId();

        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> answerService.createAnswer(questionId, answerCreateDTO, userId)
        );

        verify(questionRepository, times(1)).findById(questionId);
        verify(profileRepository, never()).findByUser_UserId(userId);
        verify(answerRepository, never()).save(any(Answer.class));
        verify(answerMapper, never()).toDTO(any(Answer.class));
    }

    @Test
    void shouldNotCreateAnswerAndThrowExceptionWhenProfileDoesNotExist() {
        UUID questionId = question1.getQuestionId();
        UUID userId = UUID.randomUUID();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question1));
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> answerService.createAnswer(questionId, answerCreateDTO, userId)
        );

        verify(questionRepository, times(1)).findById(questionId);
        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(answerRepository, never()).save(any(Answer.class));
        verify(answerMapper, never()).toDTO(any(Answer.class));
    }

    @Test
    void shouldNotAcceptAnswerAndThrowExceptionWhenAnswerDoesNotExist() {
        UUID answerId = UUID.randomUUID();
        UUID userId = userPerson2.getUserId();

        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> answerService.acceptAnswer(answerId, userId)
        );

        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, never()).save(any(Answer.class));
    }

    @Test
    void shouldNotAcceptAnswerAndThrowExceptionWhenUserIsNotOwnerQuestion() {
        UUID answerId = answer1.getAnswerId();
        UUID userId = userPerson2.getUserId();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> answerService.acceptAnswer(answerId, userId)
        );
        assertFalse(answer1.isAccepted());

        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, never()).save(any(Answer.class));
    }

    @Test
    void shouldNotAddLikeAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID answerId = answer1.getAnswerId();

        answer1.setAnswerLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> answerService.toggleAnswerLike(answerId, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(answerRepository, never()).findById(answerId);
        verify(answerRepository, never()).save(any(Answer.class));
    }

    @Test
    void shouldNotAddLikeAndThrowExceptionWhenAnswerDoesNotExist() {
        UUID userId = userPerson1.getUserId();
        UUID answerId = answer1.getAnswerId();

        answer1.setAnswerLikes(new ArrayList<>());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> answerService.toggleAnswerLike(answerId, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, never()).save(any(Answer.class));
    }

    @Test
    void shouldNotDeleteAnswerAndThrowExceptionWhenAnswerDoesNotExist() {
        UUID userId = userPerson2.getUserId();
        UUID answerId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson2));
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> answerService.deleteAnswerById(answerId, userId)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, never()).delete(any(Answer.class));
    }

    @Test
    void shouldNotDeleteAnswerAndThrowExceptionWhenUserIsNotOwner() {
        UUID userId = userPerson1.getUserId();
        UUID answerId = answer1.getAnswerId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> answerService.deleteAnswerById(answerId, userId)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(answerRepository, times(1)).findById(answerId);
        verify(answerRepository, never()).delete(any(Answer.class));
    }
}