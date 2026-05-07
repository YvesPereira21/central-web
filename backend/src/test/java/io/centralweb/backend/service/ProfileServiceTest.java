package io.centralweb.backend.service;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.dto.user.UserDTO;
import io.centralweb.backend.enums.ProfileType;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.mapper.ProfileMapper;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileMapper profileMapper;
    @Mock
    private UserService userService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @InjectMocks
    private ProfileService profileService;
    private User userAdmin;
    private User userPerson1;
    private User userPerson2;
    private Profile profilePerson1;
    private Profile profilePerson2;
    private ProfileCreateDTO profileCreateDTO;
    private ProfileDTO profileDTO;

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
        profilePerson1.setProfileType(ProfileType.UNDERGRADUATE);
        profilePerson1.setExpertise("QA");
        profilePerson1.setReputationScore(870);
        profilePerson1.setProfessional(false);
        profilePerson1.setUser(userPerson1);

        profilePerson2 = new Profile();
        ReflectionTestUtils.setField(profilePerson2, "profileId", UUID.randomUUID());
        profilePerson2.setName("Usuário Teste 2");
        profilePerson2.setBio("Sou um programador de testes 2");
        profilePerson2.setProfileType(ProfileType.SELFTAUGHT);
        profilePerson2.setExpertise("Desenvolvedor Web");
        profilePerson2.setReputationScore(110);
        profilePerson2.setProfessional(false);
        profilePerson2.setUser(userPerson2);

        UserDTO user1 = new UserDTO(
               "testcentralweb@gmail.com",
               "password"
        );

        profileCreateDTO = new ProfileCreateDTO(
                "Usuário Teste",
                "Sou um programador de testes",
                ProfileType.UNDERGRADUATE,
                "QA",
                user1
        );

        profileDTO = new ProfileDTO(
                profilePerson1.getProfileId(),
                profilePerson1.getName(),
                profilePerson1.getBio(),
                profilePerson1.getExpertise(),
                profilePerson1.getLevel(),
                profilePerson1.getReputationScore(),
                profilePerson1.isProfessional()
        );
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    void shouldCreateProfile() {
        when(profileRepository.save(any(Profile.class))).thenReturn(profilePerson1);
        when(profileMapper.toProfileUniqueDTO(any(Profile.class))).thenReturn(profileDTO);

        ProfileDTO result = profileService.createProfile(profileCreateDTO);

        assertNotNull(result);
        assertEquals("Usuário Teste", result.name());
        assertEquals("Sou um programador de testes", result.bio());
        assertEquals("QA", result.expertise());
        assertEquals(870, result.reputationScore());
        assertEquals("Iniciante", result.level());

        verify(userService, times(1)).verifyUserAlreadyExists(profilePerson1.getUser().getEmail());
        verify(bCryptPasswordEncoder, times(1)).encode(profileCreateDTO.user().password());
        verify(profileRepository, times(1)).save(any(Profile.class));
        verify(profileMapper, times(1)).toProfileUniqueDTO(any(Profile.class));
    }

    @Test
    void shouldReturnProfile() {
        UUID profileId = profilePerson1.getProfileId();

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));
        when(profileMapper.toProfileUniqueDTO(any(Profile.class))).thenReturn(profileDTO);

        ProfileDTO result = profileService.getProfileById(profileId);

        assertNotNull(result);
        assertEquals("Usuário Teste", result.name());
        assertEquals("Sou um programador de testes", result.bio());
        assertEquals("QA", result.expertise());
        assertEquals(870, result.reputationScore());
        assertEquals("Iniciante", result.level());

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileMapper, times(1)).toProfileUniqueDTO(profilePerson1);
    }

    @Test
    void shouldUpdateProfileWhenUserIsOwner() {
        UUID profileId = profilePerson1.getProfileId();
        UUID userId = profilePerson1.getUser().getUserId();
        ProfileUpdateDTO profileUpdated = new ProfileUpdateDTO(
                "Ronaldo",
                "Testando update",
                "Desenvolvedor backend"
        );

        ProfileDTO profileUpdatedResponse = new ProfileDTO(
                profilePerson1.getProfileId(),
                "Ronaldo",
                "Testando update",
                "Desenvolvedor backend",
                profilePerson1.getLevel(),
                profilePerson1.getReputationScore(),
                profilePerson1.isProfessional()
        );

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));
        when(profileRepository.save(any(Profile.class))).thenReturn(profilePerson1);
        when(profileMapper.toProfileUniqueDTO(any(Profile.class))).thenReturn(profileUpdatedResponse);

        ProfileDTO result = profileService.updateProfile(profileId, profileUpdated, userId);

        assertNotNull(result);
        assertEquals("Ronaldo", result.name());
        assertEquals("Testando update", result.bio());
        assertEquals("Desenvolvedor backend", result.expertise());

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileMapper, times(1)).updateProfileFromDTO(profileUpdated, profilePerson1);
        verify(profileRepository, times(1)).save(profilePerson1);
        verify(profileMapper, times(1)).toProfileUniqueDTO(profilePerson1);
    }

    @Test
    void shouldDeleteProfileWhenUserIsOwner() {
        UUID profileId = profilePerson1.getProfileId();
        UUID userId = profilePerson1.getUser().getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));

        assertDoesNotThrow(() -> profileService.deleteProfileById(profileId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).delete(profilePerson1);
    }

    @Test
    void shouldDeleteProfileWhenUserIsAdmin() {
        UUID profileId = profilePerson1.getProfileId();
        UUID userId = userAdmin.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));

        assertDoesNotThrow(() -> profileService.deleteProfileById(profileId, userId));

        verify(userRepository, times(1)).findById(userId);
        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).delete(profilePerson1);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 100, 100, Iniciante",
            "50, 150, 200, Bom",
            "100, 250, 350, Esperto",
            "500, 300, 800, Especialista",
            "800, 100, 900, Especialista"
    })
    void shouldAddPointsAndUpdateLevelCorrectly(
            long initialScore,
            long addedPoints,
            long expectedTotalScore,
            String expectedLevel
    ) {
        UUID profileId = profilePerson1.getProfileId();
        profilePerson1.setReputationScore(initialScore);

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));

        assertDoesNotThrow(() -> profileService.addPoints(profileId, addedPoints));

        assertEquals(expectedTotalScore, profilePerson1.getReputationScore());
        assertEquals(expectedLevel, profilePerson1.getLevel());

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).save(profilePerson1);
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    void shouldNotCreateProfileAndThrowExceptionWhenEmailAlreadyExists() {
        String email = profileCreateDTO.user().email();

        doThrow(new ObjectAlreadyExistsException("Email ou senha inválidos"))
                .when(userService).verifyUserAlreadyExists(email);

        assertThrows(
                ObjectAlreadyExistsException.class,
                () -> profileService.createProfile(profileCreateDTO)
        );

        verify(userService, times(1)).verifyUserAlreadyExists(email);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(profileRepository, never()).save(any(Profile.class));
        verify(profileMapper, never()).toProfileUniqueDTO(any(Profile.class));
    }

    @Test
    void shouldNotReturnProfileAndThrowExceptionWhenProfileDoesNotExist(){
        UUID profileId = UUID.randomUUID();

        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> profileService.getProfileById(profileId)
        );

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileMapper, never()).toProfileUniqueDTO(any(Profile.class));
    }

    @Test
    void shouldNotUpdateProfileAndThrowExceptionWhenProfileDoesNotExist() {
        UUID profileId = UUID.randomUUID();
        UUID userId = profilePerson1.getUser().getUserId();
        ProfileUpdateDTO profileUpdated = new ProfileUpdateDTO(
                "Ronaldo",
                "Testando update",
                "Desenvolvedor backend"
        );

        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> profileService.updateProfile(profileId, profileUpdated, userId)
        );

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileMapper, never()).updateProfileFromDTO(any(), any());
        verify(profileMapper, never()).toProfileUniqueDTO(any(Profile.class));
    }

    @Test
    void shouldNotUpdateProfileAndThrowExceptionWhenUserIsNotOwner() {
        UUID profileId = profilePerson1.getProfileId();
        UUID userId = profilePerson2.getUser().getUserId();
        ProfileUpdateDTO profileUpdated = new ProfileUpdateDTO(
                "Ronaldo",
                "Testando update",
                "Desenvolvedor backend"
        );

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> profileService.updateProfile(profileId, profileUpdated, userId)
        );

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileMapper, never()).updateProfileFromDTO(any(), any());
        verify(profileMapper, never()).toProfileUniqueDTO(any(Profile.class));
    }

    @Test
    void shouldNotDeleteProfileAndThrowExceptionWhenProfileDoesNotExist() {
        UUID profileId = UUID.randomUUID();
        UUID userId = userPerson1.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson1));
        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> profileService.deleteProfileById(profileId, userId)
        );

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, never()).delete(any(Profile.class));
    }

    @Test
    void shouldNotDeleteProfileAndThrowExceptionWhenUserIsNotOwnerAndIsNotAdmin() {
        UUID profileId = profilePerson1.getProfileId();
        UUID userId = userPerson2.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson2));
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profilePerson1));

        assertThrows(
                ProfileIsNotTheOwnerException.class,
                () -> profileService.deleteProfileById(profileId, userId)
        );

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, never()).delete(any(Profile.class));
    }

    @Test
    void shouldNotAddPointsAndThrowExceptionWhenProfileDoesNotExist() {
        UUID profileId = UUID.randomUUID();

        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> profileService.addPoints(profileId, 500L)
        );

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, never()).save(any(Profile.class));
    }
}