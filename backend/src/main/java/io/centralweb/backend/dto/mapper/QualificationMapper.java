package io.centralweb.backend.dto.mapper;

import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.model.Qualification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QualificationMapper {
    @Mapping(source = "profile.profileId", target = "profileId")
    @Mapping(source = "profile.name", target = "profileName")
    QualificationDTO toDTO(Qualification qualification);
}
