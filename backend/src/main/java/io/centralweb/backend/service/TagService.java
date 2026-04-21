package io.centralweb.backend.service;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.mapper.TagMapper;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    public TagDTO createTag(TagDTO tagDTO) {
        boolean tagExists = tagRepository.
                existsByTechnologyName(tagDTO.technologyName());
        if (tagExists) {
            throw new RuntimeException();
        }

        Tag newTag = new Tag();
        newTag.setTechnologyName(tagDTO.technologyName());
        newTag.setColor(tagDTO.color());

        return tagMapper.toDTO(tagRepository.save(newTag));
    }

    public TagDTO getTagByTechnologyName(String technologyName) {
        Tag tag = tagRepository
                .findByTechnologyNameContainingIgnoreCase(technologyName)
                .orElseThrow();

        return tagMapper.toDTO(tag);
    }

    public TagDTO updateTag(UUID tagId, TagUpdateDTO tagUpdated) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow();

        tagMapper.updateTagFromDTO(tagUpdated, tag);

        return tagMapper.toDTO(tagRepository.save(tag));
    }

    public void deleteTagByTechnologyName(String technologyName) {
        Tag tag = tagRepository
                .findByTechnologyNameContainingIgnoreCase(technologyName)
                .orElseThrow();

        tagRepository.delete(tag);
    }

    public List<Tag> convertTechnologyNamesToTags(List<String> technologyNames) {
        List<Tag> tags = new ArrayList<>();
        for(String technologyName : technologyNames) {
            Tag tag = tagRepository
                    .findByTechnologyName(technologyName)
                    .orElseThrow();
            tags.add(tag);
        }
        return tags;
    }
}

