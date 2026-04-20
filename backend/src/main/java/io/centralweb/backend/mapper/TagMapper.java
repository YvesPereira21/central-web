package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.model.Tag;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDTO toDTO(Tag tag);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "articles", ignore = true)
    @Mapping(target = "questions", ignore = true)
    void updateTagFromDTO(TagUpdateDTO dto, @MappingTarget Tag tag);
}
