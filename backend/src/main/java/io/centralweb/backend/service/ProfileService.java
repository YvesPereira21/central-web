package io.centralweb.backend.service;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.mapper.ProfileMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    public ProfileDTO createProfile(ProfileCreateDTO profile) {
        User user = new User();
        user.setUsername(profile.user().username());
        user.setEmail(profile.user().username());
        user.setPassword(profile.user().username());
        user.setRole(UserRole.PERSON);

        Profile newProfile = new Profile();
        newProfile.setBio(profile.bio());
        newProfile.setProfileType(profile.profileType());
        newProfile.setExpertise(profile.expertise());
        newProfile.setUser(user);

        return profileMapper.toProfileUniqueDTO(profileRepository.save(newProfile));
    }

    public ProfileDTO getProfileById(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow();

        return profileMapper.toProfileUniqueDTO(profile);
    }

    public ProfileDTO updateProfile(UUID profileId, ProfileUpdateDTO profileUpdated) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow();

        profileMapper.updateProfileFromDTO(profileUpdated, profile);

        return profileMapper.toProfileUniqueDTO(profileRepository.save(profile));
    }

    public void deleteProfileById(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow();

        profileRepository.delete(profile);
    }
}
