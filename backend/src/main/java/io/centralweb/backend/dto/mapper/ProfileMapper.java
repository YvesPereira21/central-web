package io.centralweb.backend.dto.mapper;

import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.model.Profile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "photo.photo_url", target = "photoUrl")
    ProfileSimpleDTO toProfileSimpleDTO(Profile profile);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "photo.photo_url", target = "photoUrl")
    ProfileDTO toProfileUniqueDTO(Profile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "articles", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "profileType", ignore = true)
    @Mapping(target = "professional", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "qualifications", ignore = true)
    @Mapping(target = "reputationScore", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateProfileFromDTO(ProfileUpdateDTO dto, @MappingTarget Profile profile);
}
