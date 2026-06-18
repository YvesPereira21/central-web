package io.centralweb.backend.service;

import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.model.Photo;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private PhotoService photoService;

    @TempDir
    Path tempDir;

    private Profile profile;
    private MultipartFile multipartFile;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(photoService, "uploadDir", tempDir.toString());

        profile = new Profile();
        ReflectionTestUtils.setField(profile, "profileId", UUID.randomUUID());
        profile.setName("User Test");

        multipartFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    // ----------------------- HAPPY PATH ------------------------------

    @Test
    void shouldUploadAvatarWhenProfileExistsAndHasNoPhoto() throws IOException {
        UUID profileId = profile.getProfileId();

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        assertDoesNotThrow(() -> photoService.uploadAvatar(profileId, multipartFile));

        assertNotNull(profile.getPhoto());
        assertTrue(profile.getPhoto().getPhoto_url().startsWith(tempDir.toString()));
        assertTrue(profile.getPhoto().getPhoto_url().endsWith("test.jpg"));

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).save(profile);

        // Check if file was physically created
        long count = Files.list(tempDir).count();
        assertEquals(1, count);
    }

    @Test
    void shouldUploadAvatarAndReplaceOldPhotoWhenProfileAlreadyHasPhoto() throws IOException {
        UUID profileId = profile.getProfileId();

        // Create a dummy old file in the temp directory
        Path oldFilePath = tempDir.resolve("old_avatar.jpg");
        Files.write(oldFilePath, "old content".getBytes());

        Photo oldPhoto = new Photo();
        oldPhoto.setPhoto_url(oldFilePath.toString());
        oldPhoto.setProfile(profile);
        profile.setPhoto(oldPhoto);

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        assertDoesNotThrow(() -> photoService.uploadAvatar(profileId, multipartFile));

        assertNotNull(profile.getPhoto());
        assertTrue(profile.getPhoto().getPhoto_url().startsWith(tempDir.toString()));
        assertTrue(profile.getPhoto().getPhoto_url().endsWith("test.jpg"));
        assertNotEquals(oldFilePath.toString(), profile.getPhoto().getPhoto_url());

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).save(profile);

        // Check if old file was deleted and new file was created
        assertFalse(Files.exists(oldFilePath));
        long count = Files.list(tempDir).count();
        assertEquals(1, count);
    }

    // ----------------------- UNHAPPY PATH ------------------------------

    @Test
    void shouldNotUploadAvatarAndThrowExceptionWhenProfileDoesNotExist() {
        UUID profileId = UUID.randomUUID();

        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> photoService.uploadAvatar(profileId, multipartFile));

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, never()).save(any(Profile.class));
    }
}
