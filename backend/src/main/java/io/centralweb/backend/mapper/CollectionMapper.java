package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.collection.CollectionDTO;
import io.centralweb.backend.model.Collection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ArticleMapper.class, QuestionMapper.class})
public interface CollectionMapper {
    CollectionDTO toDTO(Collection collection);
}
