package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.CollectionControllerDoc;
import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.dto.collection.CollectionDTO;
import io.centralweb.backend.service.CollectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/collections")
public class CollectionController implements CollectionControllerDoc {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping
    public ResponseEntity<CollectionDTO> createCollection(
            @RequestBody CollectionCreateDTO dto,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.createCollection(dto, userId));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @GetMapping("/my-collections")
    public ResponseEntity<Page<CollectionDTO>> getMyCollections(
            @AuthenticationPrincipal(expression = "userId") UUID userId,
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(collectionService.getMyCollections(userId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDTO> getCollectionById(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        return ResponseEntity.ok(collectionService.getCollectionById(collectionId, userId));
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/{collectionId}/articles/{articleId}")
    public ResponseEntity<Void> addArticleToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.addArticleToCollection(collectionId, articleId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @PostMapping("/{collectionId}/questions/{questionId}")
    public ResponseEntity<Void> addQuestionToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.addQuestionToCollection(collectionId, questionId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<Void> removeArticleFromAllMyCollections(
            @PathVariable UUID articleId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.removeArticleFromAllMyCollections(articleId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('PERSON')")
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> removeQuestionFromAllMyCollections(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId
    ) {
        collectionService.removeQuestionFromAllMyCollections(questionId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        collectionService.deleteCollectionById(collectionId, userId);
        return ResponseEntity.noContent().build();
    }
}
