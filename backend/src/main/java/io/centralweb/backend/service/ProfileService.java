package io.centralweb.backend.service;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.dto.mapper.ProfileMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository, ProfileMapper profileMapper, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional(rollbackOn = Exception.class)
    public ProfileDTO createProfile(ProfileCreateDTO profile) {
        log.info("Criando perfil para o usuário: {}", profile.user().email());
        userService.verifyUserAlreadyExists(profile.user().email());

        User user = new User();
        user.setEmail(profile.user().email());
        user.setPassword(bCryptPasswordEncoder.encode(profile.user().password()));
        user.setRole(UserRole.PERSON);

        Profile newProfile = new Profile();
        newProfile.setName(profile.name());
        newProfile.setBio(profile.bio());
        newProfile.setProfileType(profile.profileType());
        newProfile.setUser(user);

        Profile savedProfile = profileRepository.save(newProfile);
        log.info("Perfil criado com sucesso para o usuário: {}", profile.user().email());
        return profileMapper.toProfileUniqueDTO(savedProfile);
    }

    @Cacheable(value = "profiles", key = "#profileId")
    public ProfileDTO getProfileById(UUID profileId) {
        log.debug("Buscando perfil por ID: {}", profileId);
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        return profileMapper.toProfileUniqueDTO(profile);
    }

    @Cacheable(value = "profiles", key = "#userId")
    public ProfileDTO getProfileByUserId(UUID userId) {
        log.debug("Buscando perfil por ID de usuário: {}", userId);
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado para este usuário"));
        return profileMapper.toProfileUniqueDTO(profile);
    }

    @Caching(
            put = {
                    @CachePut(value = "profiles", key = "#result.profileId()"),
                    @CachePut(value = "profiles", key = "#result.userId()")
            },
            evict = {
                    @CacheEvict(value = "articles", allEntries = true),
                    @CacheEvict(value = "questions", allEntries = true)
            }
    )
    public ProfileDTO updateProfile(UUID profileId, ProfileUpdateDTO profileUpdated, UUID userProfileId) {
        log.info("Atualizando perfil com ID: '{}' solicitado pelo perfil de usuário com ID: '{}'", profileId, userProfileId);
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        if(!profile.getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        profileMapper.updateProfileFromDTO(profileUpdated, profile);

        Profile savedProfile = profileRepository.save(profile);
        log.info("Perfil com ID: '{}' atualizado com sucesso", profileId);
        return profileMapper.toProfileUniqueDTO(savedProfile);
    }

    @CacheEvict(value = "profiles", allEntries = true)
    public void deleteProfileById(UUID profileId, UUID userProfileId) {
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        if(!profile.getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        profileRepository.delete(profile);
    }

    @Transactional(rollbackOn = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "profiles", allEntries = true),
            @CacheEvict(value = "articles", allEntries = true),
            @CacheEvict(value = "questions", allEntries = true)
    })
    public void addPoints(UUID profileId, Long amountPoints) {
        log.info("Adicionando {} pontos ao perfil com ID: '{}'", amountPoints, profileId);
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        long points = profile.getReputationScore() + amountPoints;
        if (points < 0){
            points = 0;
        }

        profile.setReputationScore(points);
        profile.setLevel(updateLevel(points));
        log.info("Pontos adicionados com sucesso ao perfil com ID: '{}'. Nova reputação: {}. Novo nível: {}", profileId, points, profile.getLevel());
        profileRepository.save(profile);
    }

    private String updateLevel(long score){
        if (score >= 5000) {
            return "Compilador Humano";
        } else if (score >= 2500) {
            return "Domador de Legado";
        } else if (score >= 1000) {
            return "Visionário";
        } else if (score >= 500) {
            return "Veterano";
        } else if (score >= 150) {
            return "Praticante";
        } else {
            return "Novato";
        }
    }
}
