package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.model.Profile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileSimpleDTO toProfileSimpleDTO(Profile profile);

    ProfileDTO toProfileUniqueDTO(Profile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "articles", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "profileType", ignore = true)
    @Mapping(target = "professional", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "qualifications", ignore = true)
    @Mapping(target = "reputationScore", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateProfileFromDTO(ProfileUpdateDTO dto, @MappingTarget Profile profile);
}
