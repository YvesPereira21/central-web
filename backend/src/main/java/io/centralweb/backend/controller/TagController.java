package io.centralweb.backend.controller;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/tags")
@Validated
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("")
    public ResponseEntity<TagDTO> createTag(@RequestBody @Valid TagDTO tag) {
        TagDTO newTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable UUID tagId, @RequestBody @Valid TagUpdateDTO tagUpdated) {
        TagDTO tag = tagService.updateTag(tagId, tagUpdated);
        return ResponseEntity.status(HttpStatus.OK).body(tag);
    }

    @DeleteMapping("/{technologyName}")
    public ResponseEntity<Void> deleteTag(@PathVariable String technologyName) {
        tagService.deleteTagByTechnologyName(technologyName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
