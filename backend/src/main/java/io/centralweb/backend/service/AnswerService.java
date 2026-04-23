package io.centralweb.backend.service;

import io.centralweb.backend.dto.answer.AnswerAcceptedDTO;
import io.centralweb.backend.dto.answer.AnswerCreateDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.mapper.AnswerMapper;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;
    private final QuestionRepository questionRepository;
    private final ProfileRepository profileRepository;

    public AnswerService(AnswerRepository answerRepository, AnswerMapper answerMapper, QuestionRepository questionRepository, ProfileRepository profileRepository) {
        this.answerRepository = answerRepository;
        this.answerMapper = answerMapper;
        this.questionRepository = questionRepository;
        this.profileRepository = profileRepository;
    }

    public AnswerDTO createAnswer(AnswerCreateDTO answer, UUID userProfileId) {
        Question question = questionRepository.findById(answer.questionId())
                .orElseThrow();
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        Answer newAnswer = new Answer();
        newAnswer.setContent(answer.content());
        newAnswer.setCreatedAt(LocalDate.now());
        newAnswer.setQuestion(question);
        newAnswer.setProfile(profile);

        return answerMapper.toDTO(answerRepository.save(newAnswer));
    }

    public List<AnswerDTO> getAllAnswersFromQuestion(UUID questionId) {
        return answerRepository
                .findAllByQuestionIdAndQuestionPublished(questionId)
                .stream()
                .map(answerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void acceptAnswer(AnswerAcceptedDTO answerAccepted, UUID userProfileId) {
        Answer answer = answerRepository.findById(answerAccepted.answerId())
                .orElseThrow();

        if(answer.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new RuntimeException();
        }

        answer.setAccepted(true);
        answerRepository.save(answer);
    }

    public void deleteAnswerById(UUID answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow();

        answerRepository.delete(answer);
    }
}
