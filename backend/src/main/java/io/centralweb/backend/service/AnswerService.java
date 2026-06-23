package io.centralweb.backend.service;

import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.event.AnswerAcceptedEvent;
import io.centralweb.backend.event.AnswerUnacceptedEvent;
import io.centralweb.backend.exception.QuestionHaveAnswerAlreadyAcceptedException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.dto.mapper.AnswerMapper;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher publisher;

    public AnswerService(AnswerRepository answerRepository, AnswerMapper answerMapper, QuestionRepository questionRepository, UserRepository userRepository, ProfileRepository profileRepository, ApplicationEventPublisher publisher) {
        this.answerRepository = answerRepository;
        this.answerMapper = answerMapper;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.publisher = publisher;
    }

    @Transactional(rollbackOn = Exception.class)
    public AnswerDTO createAnswer(UUID questionId, AnswerCreateDTO answer, UUID userProfileId) {
        log.info("Criando resposta para a pergunta com ID: '{}' pelo perfil de usuário com ID: '{}'", questionId, userProfileId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Answer newAnswer = new Answer();
        newAnswer.setContent(answer.content());
        newAnswer.setCreatedAt(LocalDate.now());
        newAnswer.setQuestion(question);
        newAnswer.setProfile(profile);

        Answer savedAnswer = answerRepository.save(newAnswer);
        log.info("Resposta criada com sucesso com o ID: '{}'", savedAnswer.getAnswerId());
        return answerMapper.toDTO(savedAnswer);
    }

    public Page<AnswerDTO> getAllAnswersFromQuestion(UUID questionId, Pageable pageable) {
        log.debug("Buscando página {} de respostas para a pergunta com ID: {}", pageable.getPageNumber(), questionId);
        return answerRepository
                .findAllByQuestionIdAndQuestionPublished(questionId, pageable)
                .map(answerMapper::toDTO);
    }

    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(value = "questions", allEntries = true)
    public void acceptAnswer(UUID answerId, UUID userProfileId) {
        log.info("Aceitando resposta com ID: '{}' solicitada pelo perfil de usuário com ID: '{}'", answerId, userProfileId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ObjectNotFoundException("Resposta não encontrada"));

        if(answer.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        boolean questionHaveAnswerAccepted = answerRepository
                .existsByQuestion_QuestionIdAndAcceptedTrue(answer.getQuestion().getQuestionId());
        if(questionHaveAnswerAccepted){
            throw new QuestionHaveAnswerAlreadyAcceptedException("Não é possível aceitar mais de 1 resposta");
        }

        answer.setAccepted(true);
        answer.getQuestion().setSolutioned(true);
        questionRepository.save(answer.getQuestion());

        Answer answerAccepted = answerRepository.save(answer);
        log.info("Resposta com ID: '{}' aceita com sucesso", answerId);
        publisher.publishEvent(new AnswerAcceptedEvent(answer.getProfile().getProfileId()));

        answerMapper.toDTO(answerAccepted);
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

    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(value = "questions", allEntries = true)
    public void deleteAnswerById(UUID answerId, UUID userProfileId) {
        log.info("Excluindo resposta com ID: '{}' solicitada pelo perfil de usuário com ID: '{}'", answerId, userProfileId);
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ObjectNotFoundException("Resposta não encontrada"));

        if(!answer.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)){
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        if(answer.isAccepted()){
            answer.getQuestion().setSolutioned(false);
            questionRepository.save(answer.getQuestion());
            publisher.publishEvent(new AnswerUnacceptedEvent(answer.getProfile().getProfileId()));
        }

        answerRepository.delete(answer);
        log.info("Resposta com ID: '{}' excluída com sucesso", answerId);
    }
}
