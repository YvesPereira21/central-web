package io.centralweb.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProfileType {
    PROFESSIONAL("profissional"),
    UNDERGRADUATE("universitario"),
    SELFTAUGHT("autodidata");

    private final String profileType;

    ProfileType(String profileType) {
        this.profileType = profileType;
    }

    @JsonValue
    public String getProfileType() {
        return profileType;
    }

    @JsonCreator
    public static ProfileType fromValue(String value) {
        for(ProfileType profileType : values()) {
            if (profileType.profileType.equalsIgnoreCase(value) ||
                    profileType.name().equalsIgnoreCase(value)) {
                return profileType;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

