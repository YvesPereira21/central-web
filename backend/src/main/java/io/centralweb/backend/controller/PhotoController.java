package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.PhotoControllerDoc;
import io.centralweb.backend.service.PhotoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/photos")
public class PhotoController implements PhotoControllerDoc {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @Override
    @PostMapping(value = "/{profileId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(
            @PathVariable UUID profileId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            photoService.uploadAvatar(profileId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
