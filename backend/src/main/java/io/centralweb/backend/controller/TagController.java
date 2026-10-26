package io.centralweb.backend.controller;

import io.centralweb.backend.controller.doc.TagControllerDoc;
import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/tags")
@Validated
public class TagController implements TagControllerDoc {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tag) {
        TagDTO newTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSON')")
    @GetMapping("")
    public ResponseEntity<Page<TagDTO>> getAllTags(
            @PageableDefault(page = 0, size = 10, sort = "technologyName", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(tagService.getAllTags(pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{tagId}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable UUID tagId,
            @RequestBody TagUpdateDTO tagUpdated
    ) {
        TagDTO tag = tagService.updateTag(tagId, tagUpdated);
        return ResponseEntity.status(HttpStatus.OK).body(tag);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{technologyName}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable String technologyName
    ) {
        tagService.deleteTagByTechnologyName(technologyName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
