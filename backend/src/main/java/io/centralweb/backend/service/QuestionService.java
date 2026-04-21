package io.centralweb.backend.service;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.mapper.QuestionMapper;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final TagService tagService;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, TagService tagService) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.tagService = tagService;
    }

    public QuestionDTO createQuestion(QuestionCreateDTO questionUniqueDTO) {
        Question question = new Question();
        question.setTitle(questionUniqueDTO.title());
        question.setContent(questionUniqueDTO.content());
        question.setCreatedAt(LocalDate.now());
        question.setTags(tagService.convertTechnologyNamesToTags(questionUniqueDTO.technologyNames()));

        return questionMapper.toQuestionDTO(questionRepository.save(question));
    }

    public QuestionDTO getQuestionById(UUID questionId) {
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow();
        return questionMapper.toQuestionDTO(question);
    }

    public List<QuestionListDTO> getAllPublishedQuestionsByTitle(String title){
        return questionRepository
                .findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(title)
                .stream()
                .map(questionMapper::toQuestionListDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionListDTO> getAllPublishedQuestionsByTechnologyName(
            String technologyName
    ) {
        return questionRepository
                .findAllByTags_TechnologyNameAndPublishedIsTrue(technologyName)
                .stream()
                .map(questionMapper::toQuestionListDTO)
                .toList();
    }

    public List<QuestionListDTO> getAllPublishedQuestionWithAcceptedAnswer(){
        return questionRepository
                .findPublishedQuestionsWithAcceptedAnswers()
                .stream()
                .map(questionMapper::toQuestionListDTO)
                .toList();
    }

    public QuestionDTO updateQuestion(UUID questionId, QuestionUpdateDTO questionUpdated) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow();

        questionMapper.updateQuestionFromDTO(questionUpdated, question);

        return questionMapper.toQuestionDTO(questionRepository.save(question));
    }

    public void deleteQuestionById(UUID questionId) {
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow();

        questionRepository.delete(question);
    }
}
