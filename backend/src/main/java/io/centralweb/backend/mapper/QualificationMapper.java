package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.model.Qualification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QualificationMapper {
    QualificationDTO toDTO(Qualification qualification);
}
