package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.service.TagService;
import org.mapstruct.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@Mapper(componentModel = "spring", uses = {TagMapper.class, ProfileMapper.class})
public interface ArticleMapper {
    @Mapping(target = "liked", expression = "java(verifyArticleLikedByCurrentUser(article))")
    ArticleDTO toDTO(Article article);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "articleLikes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateArticleFromDTO(ArticleUpdateDTO dto, @MappingTarget Article article);

    default boolean verifyArticleLikedByCurrentUser(Article article) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !Objects.equals(auth.getPrincipal(), "anonymousUser")) {
            String emailUser = auth.getName();

            // procura se o e-mail do usuario autenticado está na lista de curtidas
            return article.getArticleLikes().stream()
                    .anyMatch(perfil -> perfil.getUser().getEmail().equals(emailUser));
        }
        //se o usuario nem está logado, então é false automaticamente
        return false;
    }
}

