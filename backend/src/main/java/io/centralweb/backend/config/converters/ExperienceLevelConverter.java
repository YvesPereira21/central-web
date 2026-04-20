package io.centralweb.backend.config.converters;

import io.centralweb.backend.enums.ExperienceLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExperienceLevelConverter implements AttributeConverter<ExperienceLevel, String> {

    @Override
    public String convertToDatabaseColumn(ExperienceLevel experienceLevel) {
        return experienceLevel != null ? experienceLevel.getExperienceLevel() : null;
    }

    @Override
    public ExperienceLevel convertToEntityAttribute(String value) {
        return value != null ? ExperienceLevel.fromValue(value) : null;
    }
}

