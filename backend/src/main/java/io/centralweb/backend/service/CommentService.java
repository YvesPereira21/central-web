package io.centralweb.backend.service;

import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.CommentMapper;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.Comment;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.CommentRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, AnswerRepository answerRepository, UserRepository userRepository, ProfileRepository profileRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public CommentDTO createComment(UUID answerId, CommentCreateDTO commentDTO, UUID userProfileId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ObjectNotFoundException("Resposta não encontrada"));
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Comment newComment = new Comment();
        newComment.setContent(commentDTO.content());
        newComment.setCreatedAt(LocalDate.now());
        newComment.setAnswer(answer);
        newComment.setProfile(profile);

        return commentMapper.toDTO(commentRepository.save(newComment));
    }

    public Page<CommentDTO> getAllCommentsFromAnswer(UUID answerId, Pageable pageable) {
        return commentRepository
                .findAllByAnswer_AnswerId(answerId, pageable)
                .map(commentMapper::toDTO);
    }

    @Transactional(rollbackOn = Exception.class)
    public CommentDTO updateComment(UUID commentId, CommentUpdateDTO dto, UUID userProfileId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Comentário não encontrado"));

        if (!comment.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para editar este comentário");
        }

        commentMapper.updateCommentFromDTO(dto, comment);
        return commentMapper.toDTO(commentRepository.save(comment));
    }

    public void deleteCommentById(UUID commentId, UUID userProfileId) {
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Comentário não encontrado"));

        if(!comment.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)){
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        commentRepository.delete(comment);
    }
}
