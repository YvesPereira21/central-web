package io.centralweb.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExperienceLevel {
    JUNIOR("JR"),
    MID("PL"),
    SENIOR("SR");

    private final String experienceLevel;

    ExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    @JsonValue
    public String getExperienceLevel() {
        return this.experienceLevel;
    }

    @JsonCreator
    public static ExperienceLevel fromValue(String value) {
        for(ExperienceLevel experienceLevel: values()) {
            if(experienceLevel.experienceLevel.equalsIgnoreCase(value) ||
                    experienceLevel.name().equalsIgnoreCase(value)) {
                return experienceLevel;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

