package io.centralweb.backend.service;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.enums.ExperienceLevel;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.events.QualificationCreateEvent;
import io.centralweb.backend.events.QualificationDeleteEvent;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.exception.InvalidDateException;
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

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
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
        log.info("Criando nova qualificação para o perfil de usuário com ID: '{}'", userProfileId);
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        boolean dateIsValid = dateIsValid(qualification.startDate(), qualification.endDate());
        if (!dateIsValid) {
            log.warn("Intervalo de datas inválido fornecido para criação de qualificação: inicio={}, fim={}", qualification.startDate(), qualification.endDate());
            throw new InvalidDateException("Data inválida. Coloque uma data válida");
        }

        Qualification newQualification = new Qualification();
        newQualification.setJobTitle(qualification.jobTitle());
        newQualification.setExperienceLevel(qualification.experienceLevel());
        newQualification.setInstitution(qualification.institution());
        newQualification.setStartDate(qualification.startDate());
        newQualification.setEndDate(qualification.endDate());
        newQualification.setProfile(profile);

        Qualification qualificationCreated = qualificationRepository.save(newQualification);
        log.info("Qualificação criada com sucesso com o ID: '{}'", qualificationCreated.getQualificationId());
        publisher.publishEvent(
                new QualificationCreateEvent(
                        qualificationCreated.getProfile().getProfileId(),
                        qualificationCreated.getExperienceLevel()
                )
        );

        return qualificationMapper.toDTO(qualificationCreated);
    }

    public Page<QualificationDTO> getAllQualificationsVerified(Pageable pageable) {
        log.debug("Buscando página {} de qualificações verificadas", pageable.getPageNumber());
        return qualificationRepository
                .findAllByVerifiedIsTrue(pageable)
                .map(qualificationMapper::toDTO);
    }

    public Page<QualificationDTO> getAllNotVerifiedQualifications(Pageable pageable) {
        log.debug("Buscando página {} de qualificações não verificadas", pageable.getPageNumber());
        return qualificationRepository
                .findAllByVerifiedIsFalse(pageable)
                .map(qualificationMapper::toDTO);
    }

    public Page<QualificationDTO> getAllProfileVerifiedQualifications(UUID profileId, Pageable pageable){
        log.debug("Buscando qualificações verificadas para o perfil com ID: {} página {}", profileId, pageable.getPageNumber());
        return qualificationRepository
                .findAllByProfile_ProfileIdAndVerifiedIsTrue(profileId, pageable)
                .map(qualificationMapper::toDTO);
    }

    public Page<QualificationDTO> getAllProfileNotVerifiedQualifications(UUID profileId, Pageable pageable){
        log.debug("Buscando qualificações não verificadas para o perfil com ID: {} página {}", profileId, pageable.getPageNumber());
        return qualificationRepository
                .findAllByProfile_ProfileIdAndVerifiedIsFalse(profileId, pageable)
                .map(qualificationMapper::toDTO);
    }

    public void markAsVerified(UUID qualificationId) {
        log.info("Marcando qualificação com ID: '{}' como verificada", qualificationId);
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        qualification.setVerified(true);
        qualificationRepository.save(qualification);
        log.info("Qualificação com ID: '{}' marcada como verificada com sucesso", qualificationId);
    }

    public void deleteQualificationById(UUID qualificationId, UUID userProfileId){
        log.info("Excluindo qualificação com ID: '{}' solicitada pelo perfil de usuário com ID: '{}'", qualificationId, userProfileId);
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ObjectNotFoundException("Currículo não encontrado"));

        if(!qualification.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)) {
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        qualificationRepository.delete(qualification);
        log.info("Qualificação com ID: '{}' excluída com sucesso", qualificationId);

        publisher.publishEvent(new QualificationDeleteEvent(
                qualification.getProfile().getProfileId(), qualification.getExperienceLevel())
        );
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

    private boolean dateIsValid(LocalDate startDate, LocalDate endDate) {
        boolean isBefore = false;
        if (endDate != null) {
            isBefore = endDate.isBefore(startDate);
        }
        boolean isFutureDate = startDate.isAfter(LocalDate.now());

        return !(isBefore || isFutureDate);
    }
}
