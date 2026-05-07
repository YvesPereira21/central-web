package io.centralweb.backend.service;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.enums.ExperienceLevel;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualificationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private QualificationRepository qualificationRepository;
    @Mock
    private QualificationMapper qualificationMapper;
    @InjectMocks
    private QualificationService qualificationService;
    private User userAdmin;
    private User userPerson1;
    private User userPerson2;
    private Profile profilePerson1;
    private Profile profilePerson2;
    private Qualification qualification;
    private Qualification qualification2;
    private QualificationDTO qualificationDTO;
    private QualificationDTO qualificationDTO2;
    private QualificationCreateDTO qualificationCreateDTO;

    @BeforeEach
    void setUp() {
        userAdmin = new User();
        ReflectionTestUtils.setField(userAdmin, "userId", UUID.randomUUID());
        userAdmin.setEmail("testcentraldev@gmail.com");
        userAdmin.setPassword("password");
        userAdmin.setRole(UserRole.ADMIN);

        userPerson1 = new User();
        ReflectionTestUtils.setField(userPerson1, "userId", UUID.randomUUID());
        userPerson1.setEmail("testcentralweb@gmail.com");
        userPerson1.setPassword("password");
        userPerson1.setRole(UserRole.PERSON);

        userPerson2 = new User();
        ReflectionTestUtils.setField(userPerson2, "userId", UUID.randomUUID());
        userPerson2.setEmail("testcentraljunior@gmail.com");
        userPerson2.setPassword("password");
        userPerson2.setRole(UserRole.PERSON);

        profilePerson1 = new Profile();
        ReflectionTestUtils.setField(profilePerson1, "profileId", UUID.randomUUID());
        profilePerson1.setName("Usuário Teste");
        profilePerson1.setBio("Sou um programador de testes");
        profilePerson1.setUser(userPerson1);

        profilePerson2 = new Profile();
        ReflectionTestUtils.setField(profilePerson2, "profileId", UUID.randomUUID());
        profilePerson2.setName("Usuário Teste 2");
        profilePerson2.setBio("Sou um programador de testes 2");
        profilePerson2.setUser(userPerson2);

        qualification = new Qualification();
        ReflectionTestUtils.setField(qualification, "qualificationId", UUID.randomUUID());
        qualification.setJobTitle("Desenvolvedor Backend");
        qualification.setExperienceLevel(ExperienceLevel.JUNIOR);
        qualification.setInstitution("Itaú");
        qualification.setStartDate(LocalDate.of(2021, 4, 23));
        qualification.setEndDate(LocalDate.of(2022, 6, 17));
        qualification.setVerified(true);
        qualification.setProfile(profilePerson1);

        qualification2 = new Qualification();
        ReflectionTestUtils.setField(qualification2, "qualificationId", UUID.randomUUID());
        qualification2.setJobTitle("Desenvolvedor Backend");
        qualification2.setExperienceLevel(ExperienceLevel.JUNIOR);
        qualification2.setInstitution("Americanas");
        qualification2.setStartDate(LocalDate.of(2022, 8, 11));
        qualification2.setEndDate(LocalDate.of(2023, 9, 8));
        qualification2.setVerified(false);
        qualification2.setProfile(profilePerson1);

        qualificationCreateDTO = new QualificationCreateDTO(
        "Desenvolvedor Backend",
        ExperienceLevel.JUNIOR,
        "Itaú",
        LocalDate.of(2021, 4, 23),
        LocalDate.of(2022, 6, 17)
        );

        qualificationDTO = new QualificationDTO(
                qualification.getJobTitle(),
                qualification.getExperienceLevel(),
                qualification.getInstitution(),
                qualification.getStartDate(),
                qualification.getEndDate()
        );

        qualificationDTO2 = new QualificationDTO(
                qualification2.getJobTitle(),
                qualification2.getExperienceLevel(),
                qualification2.getInstitution(),
                qualification2.getStartDate(),
                qualification2.getEndDate()
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateQualification() {
        UUID userId = userPerson1.getUserId();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profilePerson1));
        when(qualificationRepository.save(any(Qualification.class))).thenReturn(qualification);
        when(qualificationMapper.toDTO(qualification)).thenReturn(qualificationDTO);

        QualificationDTO result = qualificationService.createQualification(qualificationCreateDTO, userId);

        assertNotNull(result);
        assertEquals("Desenvolvedor Backend", result.jobTitle());
        assertEquals(ExperienceLevel.JUNIOR, result.experienceLevel());
        assertEquals("Itaú", result.institution());
        assertEquals(LocalDate.of(2021, 4, 23), result.startDate());
        assertEquals(LocalDate.of(2022, 6, 17), result.endDate());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(qualificationRepository, times(1)).save(any(Qualification.class));
        verify(qualificationMapper, times(1)).toDTO(any(Qualification.class));
    }

    @Test
    void shouldReturnQualificationsVerified() {
        List<Qualification> qualificationsVerified = List.of(qualification);
        Page<Qualification> qualificationsVerifiedPage = new PageImpl<>(qualificationsVerified);

        when(qualificationRepository.findAllByVerifiedIsTrue(any(Pageable.class)))
                .thenReturn(qualificationsVerifiedPage);
        when(qualificationMapper.toDTO(any(Qualification.class))).thenReturn(qualificationDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QualificationDTO> result = qualificationService.getAllQualificationsVerified(pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(qualificationRepository, times(1)).findAllByVerifiedIsTrue(any(Pageable.class));
        verify(qualificationMapper, times(1)).toDTO(any(Qualification.class));
    }

    @Test
    void shouldReturnNotVerifiedQualifications() {
        List<Qualification> qualificationNotVerified = List.of(qualification2);
        Page<Qualification> qualificationNotVerifiedPage = new PageImpl<>(qualificationNotVerified);

        when(qualificationRepository.findAllByVerifiedIsFalse(any(Pageable.class)))
                .thenReturn(qualificationNotVerifiedPage);
        when(qualificationMapper.toDTO(any(Qualification.class))).thenReturn(qualificationDTO2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QualificationDTO> result = qualificationService.getAllNotVerifiedQualifications(pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(qualificationRepository, times(1))
                .findAllByVerifiedIsFalse(any(Pageable.class));
        verify(qualificationMapper, times(1)).toDTO(any(Qualification.class));
    }

    @Test
    void shouldReturnProfileVerifiedQualifications() {
        UUID profileId = profilePerson1.getProfileId();
        List<Qualification> qualificationsVerified = List.of(qualification);
        Page<Qualification> qualificationsVerifiedPage = new PageImpl<>(qualificationsVerified);

        when(qualificationRepository.findAllByProfile_ProfileIdAndVerifiedIsTrue(eq(profileId), any(Pageable.class)))
                .thenReturn(qualificationsVerifiedPage);
        when(qualificationMapper.toDTO(any(Qualification.class))).thenReturn(qualificationDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QualificationDTO> result = qualificationService.getAllProfileVerifiedQualifications(profileId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(qualificationRepository, times(1))
                .findAllByProfile_ProfileIdAndVerifiedIsTrue(eq(profileId), any(Pageable.class));
        verify(qualificationMapper, times(1)).toDTO(any(Qualification.class));
    }

    @Test
    void shouldReturnProfileNotVerifiedQualifications() {
        UUID profileId = profilePerson1.getProfileId();
        List<Qualification> qualificationNotVerified = List.of(qualification2);
        Page<Qualification> qualificationNotVerifiedPage = new PageImpl<>(qualificationNotVerified);

        when(qualificationRepository.findAllByProfile_ProfileIdAndVerifiedIsFalse(eq(profileId), any(Pageable.class)))
                .thenReturn(qualificationNotVerifiedPage);
        when(qualificationMapper.toDTO(any(Qualification.class))).thenReturn(qualificationDTO2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QualificationDTO> result = qualificationService
                .getAllProfileNotVerifiedQualifications(profileId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(qualificationRepository, times(1))
                .findAllByProfile_ProfileIdAndVerifiedIsFalse(eq(profileId), any(Pageable.class));
        verify(qualificationMapper, times(1)).toDTO(any(Qualification.class));
    }

    @Test
    void shouldMarkQualificationAsVerified() {
        UUID qualificationId = qualification2.getQualificationId();

        when(qualificationRepository.findById(qualificationId)).thenReturn(Optional.of(qualification2));
        when(qualificationRepository.save(any(Qualification.class))).thenReturn(qualification2);

        assertDoesNotThrow(() -> qualificationService.markAsVerified(qualificationId));
        assertTrue(qualification2.isVerified());

        verify(qualificationRepository, times(1)).findById(qualificationId);
        verify(qualificationRepository, times(1)).save(qualification2);
    }

    @Test
    void shouldDeleteQualificationWhenUserIsOwner() {
        UUID qualificationId = qualification.getQualificationId();
        UUID userId = userPerson1.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(qualificationRepository.findById(qualificationId)).thenReturn(Optional.of(qualification));

        assertDoesNotThrow(() -> qualificationService.deleteQualificationById(qualificationId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(qualificationRepository, times(1)).findById(qualificationId);
        verify(qualificationRepository, times(1)).delete(qualification);
    }

    @Test
    void shouldDeleteQualificationWhenUserIsAdmin() {
        UUID qualificationId = qualification.getQualificationId();
        UUID userId = userAdmin.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(qualificationRepository.findById(qualificationId)).thenReturn(Optional.of(qualification));

        assertDoesNotThrow(() -> qualificationService.deleteQualificationById(qualificationId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(qualificationRepository, times(1)).findById(qualificationId);
        verify(qualificationRepository, times(1)).delete(qualification);
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateQualificationAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> qualificationService.createQualification(qualificationCreateDTO, userId)
        );

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(qualificationRepository, never()).save(any(Qualification.class));
        verify(qualificationMapper, never()).toDTO(any(Qualification.class));
    }

    @Test
    void shouldNotMarkQualificationAsVerifiedAndThrowExceptionWhenQualificationDoesNotExist(){
        UUID qualificationid = UUID.randomUUID();

        when(qualificationRepository.findById(qualificationid)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> qualificationService.markAsVerified(qualificationid)
        );

        verify(qualificationRepository, times(1)).findById(qualificationid);
        verify(qualificationRepository, never()).save(any(Qualification.class));
    }

    @Test
    void shouldNotDeleteQualificationAndThrowExceptionWhenQualificationDoesNotExist(){
        UUID qualificationid = UUID.randomUUID();
        UUID userId = userPerson1.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(qualificationRepository.findById(qualificationid)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> qualificationService.deleteQualificationById(qualificationid, userId)
        );

        verify(qualificationRepository, times(1)).findById(qualificationid);
        verify(qualificationRepository, never()).delete(any(Qualification.class));
    }

    @Test
    void shouldNotDeleteQualificationAndThrowExceptionWhenUserIsNotOwnerAndIsNotAdmin(){
        UUID qualificationid = qualification.getQualificationId();
        UUID userId = userPerson2.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(qualificationRepository.findById(qualificationid)).thenReturn(Optional.of(qualification));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> qualificationService.deleteQualificationById(qualificationid, userId)
        );

        verify(qualificationRepository, times(1)).findById(qualificationid);
        verify(qualificationRepository, never()).delete(any(Qualification.class));
    }
}