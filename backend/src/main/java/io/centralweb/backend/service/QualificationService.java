package io.centralweb.backend.service;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.enums.ExperienceLevel;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.QualificationCreateEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.QualificationMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Qualification;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QualificationRepository;
import io.centralweb.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QualificationService {
    private final QualificationRepository qualificationRepository;
    private final QualificationMapper qualificationMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher publisher;

    public QualificationService(QualificationRepository qualificationRepository, QualificationMapper qualificationMapper, UserRepository userRepository, ProfileRepository profileRepository, ApplicationEventPublisher publisher) {
        this.qualificationRepository = qualificationRepository;
        this.qualificationMapper = qualificationMapper;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.publisher = publisher;
    }

    @Transactional(rollbackOn = Exception.class)
    public QualificationDTO createQualification(QualificationCreateDTO qualification, UUID userProfileId) {
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Qualification newQualification = new Qualification();
        newQualification.setJobTitle(qualification.jobTitle());
        newQualification.setExperienceLevel(qualification.experienceLevel());
        newQualification.setInstitution(qualification.institution());
        newQualification.setStartDate(qualification.startDate());
        newQualification.setEndDate(qualification.endDate());
        newQualification.setProfile(profile);

        Qualification qualificationCreated = qualificationRepository.save(newQualification);
        publisher.publishEvent(
                new QualificationCreateEvent(
                        qualificationCreated.getProfile().getProfileId(),
                        qualificationCreated.getExperienceLevel()
                )
        );

        return qualificationMapper.toDTO(qualificationCreated);
    }

    public Page<QualificationDTO> getAllQualificationsVerified(Pageable pageable) {
        return qualificationRepository
                .findAllByVerifiedIsTrue(pageable)
                .map(qualificationMapper::toDTO);
    }

    public Page<QualificationDTO> getAllNotVerifiedQualifications(Pageable pageable) {
        return qualificationRepository
                .findAllByVerifiedIsFalse(pageable)
                .map(qualificationMapper::toDTO);
    }

    public Page<QualificationDTO> getAllProfileVerifiedQualifications(UUID profileId, Pageable pageable){
        return qualificationRepository
                .findAllByProfile_ProfileIdAndVerifiedIsTrue(profileId, pageable)
                .map(qualificationMapper::toDTO);
    }

    public Page<QualificationDTO> getAllProfileNotVerifiedQualifications(UUID profileId, Pageable pageable){
        return qualificationRepository
                .findAllByProfile_ProfileIdAndVerifiedIsFalse(profileId, pageable)
                .map(qualificationMapper::toDTO);
    }

    public void markAsVerified(UUID qualificationId) {
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        qualification.setVerified(true);
        qualificationRepository.save(qualification);
    }

    public void deleteQualificationById(UUID qualificationId, UUID userProfileId){
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        if(!qualification.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        qualificationRepository.delete(qualification);
    }

    public long getExperienceLevelAndReturnPoints(ExperienceLevel experienceLevel){
        switch (experienceLevel) {
            case JUNIOR -> {
                return 100;
            }
            case MID -> {
                return 200;
            }
            case SENIOR -> {
                return 300;
            }
            case null, default -> {
                return 0;
            }
        }
    }
}
