package io.centralweb.backend.service;

import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.dto.mapper.CommentMapper;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.Comment;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.CommentRepository;
import io.centralweb.backend.repository.ProfileRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CommentService commentService;

    private User userAdmin;
    private User userPerson;
    private User userPersonOther;
    private Profile profile;
    private Profile profileOther;
    private Answer answer;
    private Comment comment;
    private CommentCreateDTO createDTO;
    private CommentDTO commentDTO;
    private CommentUpdateDTO updateDTO;

    @BeforeEach
    public void setUp() {
        userAdmin = new User();
        ReflectionTestUtils.setField(userAdmin, "userId", UUID.randomUUID());
        userAdmin.setRole(UserRole.ADMIN);

        userPerson = new User();
        ReflectionTestUtils.setField(userPerson, "userId", UUID.randomUUID());
        userPerson.setRole(UserRole.PERSON);

        userPersonOther = new User();
        ReflectionTestUtils.setField(userPersonOther, "userId", UUID.randomUUID());
        userPersonOther.setRole(UserRole.PERSON);

        profile = new Profile();
        ReflectionTestUtils.setField(profile, "profileId", UUID.randomUUID());
        profile.setName("User 1");
        profile.setUser(userPerson);

        profileOther = new Profile();
        ReflectionTestUtils.setField(profileOther, "profileId", UUID.randomUUID());
        profileOther.setName("User 2");
        profileOther.setUser(userPersonOther);

        answer = new Answer();
        ReflectionTestUtils.setField(answer, "answerId", UUID.randomUUID());
        answer.setContent("This is an answer");

        comment = new Comment();
        ReflectionTestUtils.setField(comment, "commentId", UUID.randomUUID());
        comment.setContent("This is a comment");
        comment.setCreatedAt(LocalDate.now());
        comment.setAnswer(answer);
        comment.setProfile(profile);

        createDTO = new CommentCreateDTO("This is a comment");
        updateDTO = new CommentUpdateDTO("Updated comment");

        ProfileSimpleDTO profileSimpleDTO = new ProfileSimpleDTO(
                profile.getProfileId(),
                userPerson.getUserId(),
                "User 1",
                "Iniciante",
                false,
                null
        );

        commentDTO = new CommentDTO(
                comment.getCommentId(),
                comment.getContent(),
                comment.getCreatedAt(),
                profileSimpleDTO
        );
    }

    // ----------------------- HAPPY PATH ------------------------------

    @Test
    void shouldCreateComment() {
        UUID userId = userPerson.getUserId();
        UUID answerId = answer.getAnswerId();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDTO(any(Comment.class))).thenReturn(commentDTO);

        CommentDTO result = commentService.createComment(answerId, createDTO, userId);

        assertNotNull(result);
        assertEquals("This is a comment", result.content());

        verify(answerRepository, times(1)).findById(answerId);
        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(commentMapper, times(1)).toDTO(any(Comment.class));
    }

    @Test
    void shouldReturnAllCommentsFromAnswer() {
        UUID answerId = answer.getAnswerId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        when(commentRepository.findAllByAnswer_AnswerId(answerId, pageable)).thenReturn(commentPage);
        when(commentMapper.toDTO(any(Comment.class))).thenReturn(commentDTO);

        Page<CommentDTO> result = commentService.getAllCommentsFromAnswer(answerId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(commentRepository, times(1)).findAllByAnswer_AnswerId(answerId, pageable);
        verify(commentMapper, times(1)).toDTO(any(Comment.class));
    }

    @Test
    void shouldUpdateCommentWhenUserIsOwner() {
        UUID commentId = comment.getCommentId();
        UUID userId = userPerson.getUserId();

        CommentDTO updatedDTO = new CommentDTO(comment.getCommentId(), "Updated comment", comment.getCreatedAt(), commentDTO.profile());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDTO(any(Comment.class))).thenReturn(updatedDTO);

        CommentDTO result = commentService.updateComment(commentId, updateDTO, userId);

        assertNotNull(result);
        assertEquals("Updated comment", result.content());

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentMapper, times(1)).updateCommentFromDTO(updateDTO, comment);
        verify(commentRepository, times(1)).save(comment);
        verify(commentMapper, times(1)).toDTO(comment);
    }

    @Test
    void shouldDeleteCommentWhenUserIsOwner() {
        UUID commentId = comment.getCommentId();
        UUID userId = userPerson.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> commentService.deleteCommentById(commentId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void shouldDeleteCommentWhenUserIsAdmin() {
        UUID commentId = comment.getCommentId();
        UUID userId = userAdmin.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> commentService.deleteCommentById(commentId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(comment);
    }

    // ----------------------- UNHAPPY PATH ------------------------------

    @Test
    void shouldNotCreateCommentAndThrowExceptionWhenAnswerDoesNotExist() {
        UUID userId = userPerson.getUserId();
        UUID answerId = UUID.randomUUID();

        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.createComment(answerId, createDTO, userId));

        verify(answerRepository, times(1)).findById(answerId);
        verify(profileRepository, never()).findByUser_UserId(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldNotCreateCommentAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID answerId = answer.getAnswerId();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.createComment(answerId, createDTO, userId));

        verify(answerRepository, times(1)).findById(answerId);
        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldNotUpdateCommentAndThrowExceptionWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        UUID userId = userPerson.getUserId();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.updateComment(commentId, updateDTO, userId));

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentMapper, never()).updateCommentFromDTO(any(), any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldNotUpdateCommentAndThrowExceptionWhenUserIsNotOwner() {
        UUID commentId = comment.getCommentId();
        UUID userId = userPersonOther.getUserId();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(ProfileIsNotTheOwnerException.class, () -> commentService.updateComment(commentId, updateDTO, userId));

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentMapper, never()).updateCommentFromDTO(any(), any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldNotDeleteCommentAndThrowExceptionWhenUserDoesNotExist() {
        UUID commentId = comment.getCommentId();
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.deleteCommentById(commentId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void shouldNotDeleteCommentAndThrowExceptionWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        UUID userId = userPerson.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson));
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.deleteCommentById(commentId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void shouldNotDeleteCommentAndThrowExceptionWhenUserIsNotOwnerOrAdmin() {
        UUID commentId = comment.getCommentId();
        UUID userId = userPersonOther.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPersonOther));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(ProfileIsNotTheOwnerException.class, () -> commentService.deleteCommentById(commentId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
