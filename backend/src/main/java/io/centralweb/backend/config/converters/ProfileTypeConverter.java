package io.centralweb.backend.config.converters;

import io.centralweb.backend.enums.ProfileType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProfileTypeConverter implements AttributeConverter<ProfileType, String> {

    @Override
    public String convertToDatabaseColumn(ProfileType profileType) {
        return profileType != null ? profileType.getProfileType() : null;
    }

    @Override
    public ProfileType convertToEntityAttribute(String value) {
        return value != null ? ProfileType.fromValue(value) : null;
    }
}

