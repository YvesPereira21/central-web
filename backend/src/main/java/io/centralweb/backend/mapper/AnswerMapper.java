package io.centralweb.backend.mapper;

import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.model.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@Mapper(componentModel = "spring", uses = {ProfileMapper.class})
public interface AnswerMapper {
    @Mapping(target = "liked", expression = "java(verifyAnswerLikedByCurrentUser(answer))")
    AnswerDTO toDTO(Answer answer);

    default boolean verifyAnswerLikedByCurrentUser(Answer answer){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.isAuthenticated() && !Objects.equals(auth.getPrincipal(), "anonymousUser")){
            String emailUser = auth.getName();

            // procura se o e-mail do usuario autenticado está na lista de curtidas
            return answer.getAnswerLikes().stream()
                    .anyMatch(profile -> profile.getUser().getEmail().equals(emailUser));
        }

        //se o usuario nem está logado, então é false automaticamente
        return false;
    }
}
