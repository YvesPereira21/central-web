package io.centralweb.backend.repository;

import io.centralweb.backend.model.Qualification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QualificationRepository extends JpaRepository<Qualification, UUID> {
    Page<Qualification> findAllByVerifiedIsTrue(Pageable pageable);
    Page<Qualification> findAllByVerifiedIsFalse(Pageable pageable);
    Page<Qualification> findAllByProfile_ProfileIdAndVerifiedIsTrue(UUID profileProfileId, Pageable pageable);
    Page<Qualification> findAllByProfile_ProfileIdAndVerifiedIsFalse(UUID profileProfileId, Pageable pageable);
}
