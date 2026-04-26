package io.centralweb.backend.service;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.mapper.TagMapper;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional(rollbackOn = Exception.class)
    public TagDTO createTag(TagDTO tagDTO) {
        boolean tagExists = tagRepository.
                existsByTechnologyName(tagDTO.technologyName());
        if (tagExists) {
            throw new ObjectAlreadyExistsException("Tag com esse nome já existe");
        }

        Tag newTag = new Tag();
        newTag.setTechnologyName(tagDTO.technologyName());
        newTag.setColor(tagDTO.color());

        return tagMapper.toDTO(tagRepository.save(newTag));
    }

    public TagDTO getTagByTechnologyName(String technologyName) {
        Tag tag = tagRepository
                .findByTechnologyNameContainingIgnoreCase(technologyName)
                .orElseThrow(() -> new ObjectNotFoundException("Tag não encontrada"));

        return tagMapper.toDTO(tag);
    }

    public List<TagDTO> getAllTags(){
        return tagRepository.findAll().stream()
                .map(tagMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TagDTO updateTag(UUID tagId, TagUpdateDTO tagUpdated) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ObjectNotFoundException("Tag não encontrada"));

        tagMapper.updateTagFromDTO(tagUpdated, tag);

        return tagMapper.toDTO(tagRepository.save(tag));
    }

    public void deleteTagByTechnologyName(String technologyName) {
        Tag tag = tagRepository
                .findByTechnologyNameContainingIgnoreCase(technologyName)
                .orElseThrow(() -> new ObjectNotFoundException("Tag não encontrada"));

        tagRepository.delete(tag);
    }

    @Transactional(rollbackOn = Exception.class)
    public List<Tag> convertTechnologyNamesToTags(List<String> technologyNames) {
        List<Tag> tags = new ArrayList<>();
        for(String technologyName : technologyNames) {
            Tag tag = tagRepository
                    .findByTechnologyName(technologyName)
                    .orElseThrow(() -> new ObjectNotFoundException("Tag " + technologyName + " não encontrada"));
            tags.add(tag);
        }
        return tags;
    }
}

