package io.centralweb.backend.service;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.QuestionCreateEvent;
import io.centralweb.backend.events.QuestionDeleteEvent;
import io.centralweb.backend.events.AnswerUnacceptedEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.QuestionMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.specification.GenericSearchSpecification;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TagService tagService;
    private final ApplicationEventPublisher publisher;
    private final AnswerRepository answerRepository;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, UserRepository userRepository, ProfileRepository profileRepository, TagService tagService, ApplicationEventPublisher publisher, AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.tagService = tagService;
        this.publisher = publisher;
        this.answerRepository = answerRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(value = "questions", allEntries = true)
    public QuestionDTO createQuestion(QuestionCreateDTO questionDataDTO, UUID userProfileId) {
        log.info("Criando nova pergunta com o título: '{}' para o perfil de usuário com ID: '{}'", questionDataDTO.title(), userProfileId);
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
        log.info("Pergunta criada com sucesso com o ID: '{}' para o perfil com ID: '{}'", question.getQuestionId(), profile.getProfileId());
        publisher.publishEvent(new QuestionCreateEvent(profile.getProfileId()));

        return questionMapper.toQuestionDTO(question);
    }

    @Cacheable(value = "questions", key = "#questionId")
    public QuestionDTO getQuestionById(UUID questionId) {
        log.debug("Buscando pergunta por ID: {}", questionId);
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));
        return questionMapper.toQuestionDTO(question);
    }

    @Cacheable(value = "questions", key = "#root.methodName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<QuestionListDTO> getAllPublishedQuestions(Pageable pageable){
        log.debug("Buscando página {} de perguntas publicadas", pageable.getPageNumber());
        return questionRepository.findAllByPublishedIsTrue(pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    @Cacheable(value = "questions", key = "#root.methodName + '_' + #keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<QuestionListDTO> searchPublishedQuestions(String keyword, Pageable pageable) {
        Specification<Question> spec = GenericSearchSpecification.searchByTitleOrContent(keyword);
                
        return questionRepository.findAll(spec, pageable).map(questionMapper::toQuestionListDTO);
    }

    @Cacheable(value = "questions", key = "#root.methodName + '_' + #technologyName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<QuestionListDTO> getAllPublishedQuestionsByTechnologyName(
            String technologyName,
            Pageable pageable
    ) {
        return questionRepository
                .findAllByTags_TechnologyNameAndPublishedIsTrue(technologyName, pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    @Cacheable(value = "questions", key = "#root.methodName + '_' + (#tags != null ? #tags.toString() : 'null') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<QuestionListDTO> getAllPublishedQuestionsByTags(
            List<String> tags,
            Pageable pageable
    ) {
        if (tags == null || tags.isEmpty()) {
            return getAllPublishedQuestions(pageable);
        }
        return questionRepository
                .findAllByTagsStrict(tags, tags.size(), pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    @Cacheable(value = "questions", key = "#root.methodName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<QuestionListDTO> getAllPublishedQuestionWithAcceptedAnswer(
            Pageable pageable
    ){
        return questionRepository
                .findPublishedQuestionsWithAcceptedAnswers(pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    @Cacheable(value = "questions")
    public Page<QuestionListDTO> getAllQuestionsByProfile(UUID profileId, UUID userId, Pageable pageable) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        if (!profile.getUser().getUserId().equals(userId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para visualizar estas perguntas.");
        }

        return questionRepository
                .findAllByProfile_ProfileIdAndPublishedIsTrue(profileId, pageable)
                .map(questionMapper::toQuestionListDTO);
    }

    @CacheEvict(value = "questions", allEntries = true)
    public QuestionDTO updateQuestion(UUID questionId, QuestionUpdateDTO questionUpdated, UUID userProfileId) {
        log.info("Atualizando pergunta com ID: '{}' solicitada pelo perfil de usuário com ID: '{}'", questionId, userProfileId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        if(!question.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        questionMapper.updateQuestionFromDTO(questionUpdated, question);
        if (questionUpdated.technologyNames() != null) {
            question.setTags(tagService.convertTechnologyNamesToTags(questionUpdated.technologyNames()));
        }

        Question savedQuestion = questionRepository.save(question);
        log.info("Pergunta com ID: '{}' atualizada com sucesso", questionId);
        return questionMapper.toQuestionDTO(savedQuestion);
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

    @CacheEvict(value = "questions", allEntries = true)
    public void deleteQuestionById(UUID questionId, UUID userProfileId) {
        log.info("Excluindo pergunta com ID: '{}' solicitada pelo perfil de usuário com ID: '{}'", questionId, userProfileId);
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        if(!question.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        answerRepository.findByQuestion_QuestionIdAndAcceptedTrue(question.getQuestionId())
                .ifPresent(answer -> publisher.publishEvent(new AnswerUnacceptedEvent(answer.getProfile().getProfileId())));

        questionRepository.delete(question);
        log.info("Pergunta com ID: '{}' excluída com sucesso", questionId);

        publisher.publishEvent(new QuestionDeleteEvent(question.getProfile().getProfileId()));
    }
}
