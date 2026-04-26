package io.centralweb.backend.service;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.mapper.ProfileMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional(rollbackOn = Exception.class)
    public ProfileDTO createProfile(ProfileCreateDTO profile) {
        userService.verifyUserAlreadyExists(profile.user().email());

        User user = new User();
        user.setEmail(profile.user().email());
        user.setPassword(bCryptPasswordEncoder.encode(profile.user().password()));
        user.setRole(UserRole.PERSON);

        Profile newProfile = new Profile();
        newProfile.setName(profile.name());
        newProfile.setBio(profile.bio());
        newProfile.setProfileType(profile.profileType());
        newProfile.setExpertise(profile.expertise());
        newProfile.setUser(user);

        return profileMapper.toProfileUniqueDTO(profileRepository.save(newProfile));
    }

    public ProfileDTO getProfileById(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        return profileMapper.toProfileUniqueDTO(profile);
    }

    public ProfileDTO updateProfile(UUID profileId, ProfileUpdateDTO profileUpdated, UUID userProfileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        if(!profile.getUser().getUserId().equals(userProfileId)) {
            throw new RuntimeException("Você não tem permissão para isso");
        }

        profileMapper.updateProfileFromDTO(profileUpdated, profile);

        return profileMapper.toProfileUniqueDTO(profileRepository.save(profile));
    }

    public void deleteProfileById(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        profileRepository.delete(profile);
    }

    @Transactional(rollbackOn = Exception.class)
    public void addPoints(UUID profileId, Long amountPoints) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        long points = profile.getReputationScore() + amountPoints;
        profile.setReputationScore(points);
        profile.setLevel(updateLevel(points));
        profileRepository.save(profile);
    }

    private String updateLevel(long score){
        return switch (score) {
            case long l when l >= 800L -> "Especialista";
            case long l when l >= 300 -> "Esperto";
            case long l when l >= 200 -> "Bom";
            default -> "Iniciante";
        };
    }
}
