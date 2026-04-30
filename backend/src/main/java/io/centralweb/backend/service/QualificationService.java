package io.centralweb.backend.service;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.mapper.QualificationMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Qualification;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QualificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QualificationService {
    private final QualificationRepository qualificationRepository;
    private final QualificationMapper qualificationMapper;
    private final ProfileRepository profileRepository;

    public QualificationService(QualificationRepository qualificationRepository, QualificationMapper qualificationMapper, ProfileRepository profileRepository) {
        this.qualificationRepository = qualificationRepository;
        this.qualificationMapper = qualificationMapper;
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

        return qualificationMapper.toDTO(qualificationRepository.save(newQualification));
    }

    public List<QualificationDTO> getAllQualificationsVerified() {
        return qualificationRepository.findAllByVerifiedIsTrue()
                .stream()
                .map(qualificationMapper::toDTO)
                .toList();
    }

    public List<QualificationDTO> getAllNotVerifiedQualifications() {
        return qualificationRepository.findAllByVerifiedIsFalse()
                .stream()
                .map(qualificationMapper::toDTO)
                .toList();
    }

    public List<QualificationDTO> getAllProfileVerifiedQualifications(UUID profileId){
        return qualificationRepository.findAllByProfile_ProfileIdAndVerifiedIsTrue(profileId)
                .stream()
                .map(qualificationMapper::toDTO)
                .toList();
    }

    public List<QualificationDTO> getAllProfileNotVerifiedQualifications(UUID profileId){
        return qualificationRepository.findAllByProfile_ProfileIdAndVerifiedIsFalse(profileId)
                .stream()
                .map(qualificationMapper::toDTO)
                .toList();
    }

    public void updateVerifiedToTrue(UUID qualificationId) {
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        qualification.setVerified(true);
        qualificationRepository.save(qualification);
    }

    public void deleteQualificationById(UUID qualificationId){
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        qualificationRepository.delete(qualification);
    }
}
