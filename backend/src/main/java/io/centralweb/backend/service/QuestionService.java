package io.centralweb.backend.service;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.QuestionCreateEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.QuestionMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TagService tagService;
    private final ApplicationEventPublisher publisher;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, UserRepository userRepository, ProfileRepository profileRepository, TagService tagService, ApplicationEventPublisher publisher) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.tagService = tagService;
        this.publisher = publisher;
    }

    @Transactional(rollbackOn = Exception.class)
    public QuestionDTO createQuestion(QuestionCreateDTO questionDataDTO, UUID userProfileId) {
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Question newQuestion = new Question();
        newQuestion.setTitle(questionDataDTO.title());
        newQuestion.setContent(questionDataDTO.content());
        newQuestion.setCreatedAt(LocalDate.now());
        newQuestion.setTags(tagService.convertTechnologyNamesToTags(questionDataDTO.technologyNames()));
        newQuestion.setPublished(true);
        newQuestion.setProfile(profile);

        Question question = questionRepository.save(newQuestion);
        publisher.publishEvent(new QuestionCreateEvent(profile.getProfileId()));

        return questionMapper.toQuestionDTO(question);
    }

    public QuestionDTO getQuestionById(UUID questionId) {
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));
        return questionMapper.toQuestionDTO(question);
    }

    public Page<QuestionListDTO> getAllPublishedQuestions(Pageable pageable){
        return questionRepository.findAllByPublishedIsTrue(pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    public Page<QuestionListDTO> getAllPublishedQuestionsByTitle(
            String title,
            Pageable pageable
    ) {
        return questionRepository
                .findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(title, pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    public Page<QuestionListDTO> getAllPublishedQuestionsByTechnologyName(
            String technologyName,
            Pageable pageable
    ) {
        return questionRepository
                .findAllByTags_TechnologyNameAndPublishedIsTrue(technologyName, pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    public Page<QuestionListDTO> getAllPublishedQuestionWithAcceptedAnswer(
            Pageable pageable
    ){
        return questionRepository
                .findPublishedQuestionsWithAcceptedAnswers(pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    public QuestionDTO updateQuestion(UUID questionId, QuestionUpdateDTO questionUpdated, UUID userProfileId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        if(!question.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        questionMapper.updateQuestionFromDTO(questionUpdated, question);
        if (questionUpdated.technologyNames() != null) {
            question.setTags(tagService.convertTechnologyNamesToTags(questionUpdated.technologyNames()));
        }

        return questionMapper.toQuestionDTO(questionRepository.save(question));
    }

    public void toggleQuestionLike(UUID questionId, UUID userProfileId) {
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        if(question.getQuestionLikes().contains(profile)) {
            question.removeLike(profile);
        } else {
            question.addLike(profile);
        }

        questionRepository.save(question);
    }

    public void deleteQuestionById(UUID questionId, UUID userProfileId) {
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        //bloquei se o usuário que está fazendo a requisição não for o proprietário
        //E NEM o administrador
        if(!question.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        questionRepository.delete(question);
    }
}
