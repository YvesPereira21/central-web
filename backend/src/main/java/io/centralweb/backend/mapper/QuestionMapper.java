package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.service.TagService;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {TagMapper.class, ProfileMapper.class, AnswerMapper.class}
)
public interface QuestionMapper {
    QuestionDTO toQuestionDTO(Question question);

    QuestionListDTO toQuestionListDTO(Question questions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "questionLikes", ignore = true)
    @Mapping(target = "solutioned", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateQuestionFromDTO(QuestionUpdateDTO dto, @MappingTarget Question question);
}

