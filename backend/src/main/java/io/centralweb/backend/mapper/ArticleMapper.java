package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.service.TagService;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {TagMapper.class, ProfileMapper.class})
public interface ArticleMapper {
    ArticleDTO toDTO(Article article);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "articleLikes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateArticleFromDTO(ArticleUpdateDTO dto, @MappingTarget Article article);
}

