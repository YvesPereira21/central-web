package io.centralweb.backend.service;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.mapper.TagMapper;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public Page<TagDTO> getAllTags(Pageable pageable) {
        return tagRepository.findAll(pageable).map(tagMapper::toDTO);}

    public TagDTO updateTag(UUID tagId, TagUpdateDTO tagUpdated) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ObjectNotFoundException("Tag não encontrada"));

        if (!tag.getTechnologyName().equals(tagUpdated.technologyName()) &&
                tagRepository.existsByTechnologyName(tagUpdated.technologyName())) {
            throw new ObjectAlreadyExistsException("Esse nome já está em uso por outra tag");
        }

        tagMapper.updateTagFromDTO(tagUpdated, tag);

        return tagMapper.toDTO(tagRepository.save(tag));
    }

    public void deleteTagByTechnologyName(String technologyName) {
        Tag tag = tagRepository
                .findByTechnologyName(technologyName)
                .orElseThrow(() -> new ObjectNotFoundException("Tag não encontrada"));

        tagRepository.delete(tag);
    }

    @Transactional(rollbackOn = Exception.class)
    public Set<Tag> convertTechnologyNamesToTags(Set<String> technologyNames) {
        Set<Tag> tags = new java.util.HashSet<>();
        for(String technologyName : technologyNames) {
            Tag tag = tagRepository
                    .findByTechnologyName(technologyName)
                    .orElseThrow(() -> new ObjectNotFoundException("Tag " + technologyName + " não encontrada"));
            tags.add(tag);
        }
        return tags;
    }
}

