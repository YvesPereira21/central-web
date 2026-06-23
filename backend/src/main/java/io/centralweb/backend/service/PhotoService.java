package io.centralweb.backend.service;

import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.model.Photo;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class PhotoService {
    private final ProfileRepository profileRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public PhotoService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public void uploadAvatar(UUID profileId, MultipartFile file) throws IOException {
        log.info("Fazendo upload do avatar para o perfil com ID: '{}'", profileId);
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(()-> new ObjectNotFoundException("Profile not found"));

        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        String fileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename();
        Path filePath = path.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        String fileUrl = uploadDir + "/" + fileName;

        if (profile.getPhoto() != null) {
            String oldPhotoUrl = profile.getPhoto().getPhoto_url();
            if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                try {
                    Path oldPath = Paths.get(oldPhotoUrl);
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    log.warn("Aviso: Falha ao deletar a foto antiga: {}", oldPhotoUrl, e);
                }
            }
            profile.getPhoto().setPhoto_url(fileUrl);
        } else {
            Photo photo = new Photo();
            photo.setPhoto_url(fileUrl);
            photo.setProfile(profile);
            profile.setPhoto(photo);
        }

        profileRepository.save(profile);
        log.info("Avatar enviado com sucesso para o perfil com ID: '{}'", profileId);
    }
}
