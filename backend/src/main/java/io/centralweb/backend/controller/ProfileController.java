package io.centralweb.backend.controller;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/profiles")
@Validated
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("")
    public ResponseEntity<ProfileDTO> createProfile(
            @RequestBody @Valid ProfileCreateDTO profile
    ){
        ProfileDTO profileUniqueDTO = profileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileUniqueDTO);
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable UUID profileId){
        return ResponseEntity.status(HttpStatus.OK).body(
                profileService.getProfileById(profileId)
        );
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileDTO> updateProfile(
            @PathVariable UUID profileId,
            @RequestBody @Valid ProfileUpdateDTO profile,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                profileService.updateProfile(profileId, profile, userId)
        );
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID profileId){
        profileService.deleteProfileById(profileId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
