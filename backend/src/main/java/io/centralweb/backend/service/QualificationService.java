package io.centralweb.backend.service;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.enums.UserRole;
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

    public QualificationService(QualificationRepository qualificationRepository, QualificationMapper qualificationMapper, UserRepository userRepository, ProfileRepository profileRepository) {
        this.qualificationRepository = qualificationRepository;
        this.qualificationMapper = qualificationMapper;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
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

        return qualificationMapper
                .toDTO(qualificationRepository.save(newQualification));
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

    public void markAsVerified(UUID qualificationId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        if(!user.getRole().equals(UserRole.ADMIN)) {
            throw new RuntimeException("Usuário não é admin");
        }

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
}
