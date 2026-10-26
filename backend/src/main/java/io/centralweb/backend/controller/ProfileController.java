package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.ProfileControllerDoc;
import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/profiles")
@Validated
public class ProfileController implements ProfileControllerDoc {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    @PostMapping("")
    public ResponseEntity<ProfileDTO> createProfile(@RequestBody ProfileCreateDTO profile) {
        ProfileDTO profileUniqueDTO = profileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileUniqueDTO);
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("hasRole('PERSON')")
    public ResponseEntity<ProfileDTO> getMyProfile(@AuthenticationPrincipal(expression = "userId") UUID userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @Override
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable UUID profileId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileDTO> updateProfile(
            @PathVariable UUID profileId,
            @RequestBody ProfileUpdateDTO profile,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(profileService.updateProfile(profileId, profile, userId));
    }

    @Override
    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable UUID profileId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        profileService.deleteProfileById(profileId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
