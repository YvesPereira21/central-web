package io.centralweb.backend.service;

import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.AnswerMapper;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public AnswerService(AnswerRepository answerRepository, AnswerMapper answerMapper, QuestionRepository questionRepository, UserRepository userRepository, ProfileRepository profileRepository) {
        this.answerRepository = answerRepository;
        this.answerMapper = answerMapper;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public AnswerDTO createAnswer(AnswerCreateDTO answer, UUID userProfileId) {
        Question question = questionRepository.findById(answer.questionId())
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Answer newAnswer = new Answer();
        newAnswer.setContent(answer.content());
        newAnswer.setCreatedAt(LocalDate.now());
        newAnswer.setQuestion(question);
        newAnswer.setProfile(profile);

        return answerMapper.toDTO(answerRepository.save(newAnswer));
    }

    public Page<AnswerDTO> getAllAnswersFromQuestion(UUID questionId, Pageable pageable) {
        return answerRepository
                .findAllByQuestionIdAndQuestionPublished(questionId, pageable)
                .map(answerMapper::toDTO);
    }

    @Transactional(rollbackOn = Exception.class)
    public void acceptAnswer(UUID answerId, UUID userProfileId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ObjectNotFoundException("Resposta não encontrada"));

        if(answer.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        answer.setAccepted(true);
        answerRepository.save(answer);
    }

    public void toggleAnswerLike(UUID answerId, UUID userProfileId){
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ObjectNotFoundException("Resposta não encontrada"));

        if(answer.getAnswerLikes().contains(profile)) {
            answer.removeLike(profile);
        }else {
            answer.addLike(profile);
        }
        answerRepository.save(answer);
    }

    public void deleteAnswerById(UUID answerId, UUID userProfileId) {
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ObjectNotFoundException("Resposta não encontrada"));

        if(!answer.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)){
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        answerRepository.delete(answer);
    }
}
