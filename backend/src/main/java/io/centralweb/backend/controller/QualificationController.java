package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.QualificationControllerDoc;
import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.service.QualificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/qualifications")
@Validated
public class QualificationController implements QualificationControllerDoc {

    private final QualificationService qualificationService;

    public QualificationController(QualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("")
    public ResponseEntity<QualificationDTO> createQualification(
            @RequestBody
            QualificationCreateDTO qualification,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        QualificationDTO newQualification = qualificationService
                .createQualification(qualification, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newQualification);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/verified")
    public ResponseEntity<Page<QualificationDTO>> getVerifiedQualifications(
            @PageableDefault(page = 0, size = 10, sort = "institution", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(qualificationService.getAllQualificationsVerified(pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/not-verified")
    public ResponseEntity<Page<QualificationDTO>> getNotVerifiedQualifications(
            @PageableDefault(page = 0, size = 10, sort = "institution", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(qualificationService.getAllNotVerifiedQualifications(pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{profileId}/verified")
    public ResponseEntity<Page<QualificationDTO>> getProfileVerifiedQualifications(
            @PathVariable UUID profileId,
            @PageableDefault(page = 0, size = 10, sort = "institution", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(qualificationService.getAllProfileVerifiedQualifications(profileId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{profileId}/not-verified")
    public ResponseEntity<Page<QualificationDTO>> getProfileNotVerifiedQualifications(
            @PathVariable UUID profileId,
            @PageableDefault(page = 0, size = 10, sort = "institution", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(qualificationService.getAllProfileNotVerifiedQualifications(profileId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{qualificationId}")
    public ResponseEntity<Void> verify(
            @PathVariable UUID qualificationId
    ){
        qualificationService.markAsVerified(qualificationId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{qualificationId}")
    public ResponseEntity<Void> deleteQualification(
            @PathVariable UUID qualificationId,
            @AuthenticationPrincipal(expression = "userId")
            UUID userProfileId
    ) {
        qualificationService.deleteQualificationById(qualificationId, userProfileId);
        return ResponseEntity.noContent().build();
    }
}
