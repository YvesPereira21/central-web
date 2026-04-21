package io.centralweb.backend.repository;

import io.centralweb.backend.model.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QualificationRepository extends JpaRepository<Qualification, UUID> {
    List<Qualification> findAllByVerifiedIsTrue();
    List<Qualification> findAllByVerifiedIsFalse();
    List<Qualification> findAllByProfile_ProfileIdAndVerifiedIsTrue(
            UUID profileProfileId
    );
    List<Qualification> findAllByProfile_ProfileIdAndVerifiedIsFalse(
            UUID profileProfileId
    );
}
