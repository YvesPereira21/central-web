package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.question.QuestionListDTO;
import io.centralweb.backend.dto.question.QuestionDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
import io.centralweb.backend.model.Question;
import io.centralweb.backend.service.TagService;
import org.mapstruct.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@Mapper(
        componentModel = "spring",
        uses = {TagMapper.class, ProfileMapper.class, AnswerMapper.class}
)
public interface QuestionMapper {
    @Mapping(target = "liked", expression = "java(verifyQuestionLikedByCurrentUser(question))")
    QuestionDTO toQuestionDTO(Question question);

    @Mapping(target = "liked", expression = "java(verifyQuestionLikedByCurrentUser(questions))")
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

    default boolean verifyQuestionLikedByCurrentUser(Question question){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !Objects.equals(auth.getPrincipal(), "anonymousUser")){
            String emailUser = auth.getName();

            return question.getQuestionLikes().stream()
                    .anyMatch(profile -> profile.getUser().getEmail().equals(emailUser));
        }

        return false;
    }
}

