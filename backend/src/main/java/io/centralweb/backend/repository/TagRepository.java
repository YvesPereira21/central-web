package io.centralweb.backend.repository;

import io.centralweb.backend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    boolean existsByTechnologyName(String technologyName);
    Optional<Tag> findByTechnologyName(String technologyName);
    Optional<Tag> findByTechnologyNameContainingIgnoreCase(String technologyName);
}
