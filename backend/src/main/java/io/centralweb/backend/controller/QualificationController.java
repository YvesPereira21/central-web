package io.centralweb.backend.controller;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.dto.qualification.QualificationDTO;
import io.centralweb.backend.service.QualificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/qualifications")
@Validated
public class QualificationController {
    private final QualificationService qualificationService;

    public QualificationController(QualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    @PostMapping("")
    public ResponseEntity<QualificationDTO> createQualification(
            @RequestBody @Valid QualificationCreateDTO qualification
    ) {
        QualificationDTO newQualification = qualificationService.createQualification(qualification);
        return ResponseEntity.status(HttpStatus.CREATED).body(newQualification);
    }

    @GetMapping("/verified")
    public ResponseEntity<List<QualificationDTO>> getVerifiedQualifications(){
        return ResponseEntity.status(HttpStatus.OK).body(
                qualificationService.getAllQualificationsVerified()
        );
    }

    @GetMapping("/notVerified")
    public ResponseEntity<List<QualificationDTO>> getNotVerifiedQualifications(){
        return ResponseEntity.status(HttpStatus.OK).body(
                qualificationService.getAllNotVerifiedQualifications()
        );
    }

    @GetMapping("/{profileId}/verified")
    public ResponseEntity<List<QualificationDTO>> getProfileVerifiedQualifications(
            @PathVariable UUID profileId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                qualificationService.getAllProfileVerifiedQualifications(profileId)
        );
    }

    @GetMapping("/{profileId}/notVerified")
    public ResponseEntity<List<QualificationDTO>> getProfileNotVerifiedQualifications(
            @PathVariable UUID profileId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                qualificationService.getAllProfileNotVerifiedQualifications(profileId)
        );
    }

    @DeleteMapping("/{qualificationId}")
    public ResponseEntity<Void> deleteQualification(@PathVariable UUID qualificationId){
        qualificationService.deleteQualificationById(qualificationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
