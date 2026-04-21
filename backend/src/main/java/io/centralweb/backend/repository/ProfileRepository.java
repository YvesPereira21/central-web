package io.centralweb.backend.repository;

import io.centralweb.backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    boolean existsByProfileId(UUID profileId);
    Optional<Profile> findByUser_UserId(UUID userId);
}
