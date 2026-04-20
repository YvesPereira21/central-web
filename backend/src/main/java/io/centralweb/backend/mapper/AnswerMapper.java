package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.model.Answer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProfileMapper.class})
public interface AnswerMapper {
    AnswerDTO toDTO(Answer answer);
}
