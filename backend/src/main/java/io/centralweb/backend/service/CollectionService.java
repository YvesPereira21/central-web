package io.centralweb.backend.service;

import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.dto.collection.CollectionDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.dto.mapper.CollectionMapper;
import io.centralweb.backend.model.*;
import io.centralweb.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;

    public CollectionService(CollectionRepository collectionRepository, CollectionMapper collectionMapper, UserRepository userRepository, ProfileRepository profileRepository, ArticleRepository articleRepository, QuestionRepository questionRepository) {
        this.collectionRepository = collectionRepository;
        this.collectionMapper = collectionMapper;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.articleRepository = articleRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public CollectionDTO createCollection(CollectionCreateDTO dto, UUID userProfileId) {
        log.info("Criando nova coleção com o nome: '{}' para o perfil de usuário com ID: '{}'", dto.name(), userProfileId);
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        Collection collection = new Collection();
        collection.setName(dto.name());
        collection.setProfile(profile);

        Collection savedCollection = collectionRepository.save(collection);
        log.info("Coleção criada com sucesso com o ID: '{}'", savedCollection.getCollectionId());
        return collectionMapper.toDTO(savedCollection);
    }

    public Page<CollectionDTO> getMyCollections(UUID userProfileId, Pageable pageable) {
        log.debug("Buscando página {} de coleções para o perfil de usuário com ID: {}", pageable.getPageNumber(), userProfileId);
        Profile profile = profileRepository.findByUser_UserId(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil não encontrado"));

        return collectionRepository.findAllByProfile_ProfileId(profile.getProfileId(), pageable)
                .map(collectionMapper::toDTO);
    }

    public CollectionDTO getCollectionById(UUID collectionId, UUID userProfileId) {
        log.debug("Buscando coleção por ID: {}", collectionId);
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Coleção não encontrada"));

        if (!collection.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Coleção não encontrada");
        }

        return collectionMapper.toDTO(collection);
    }

    @Transactional(rollbackOn = Exception.class)
    public void addArticleToCollection(UUID collectionId, UUID articleId, UUID userProfileId) {
        log.info("Adicionando artigo com ID: '{}' à coleção com ID: '{}'", articleId, collectionId);
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Coleção não encontrada"));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        if (!collection.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Coleção não encontrada");
        }

        collection.addArticle(article);
        collectionRepository.save(collection);
        log.info("Artigo adicionado com sucesso à coleção com ID: '{}'", collectionId);
    }

    @Transactional(rollbackOn = Exception.class)
    public void addQuestionToCollection(UUID collectionId, UUID questionId, UUID userProfileId) {
        log.info("Adicionando pergunta com ID: '{}' à coleção com ID: '{}'", questionId, collectionId);
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Coleção não encontrada"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        if (!collection.getProfile().getUser().getUserId().equals(userProfileId)) {
            throw new ProfileIsNotTheOwnerException("Coleção não encontrada");
        }

        collection.addQuestion(question);
        collectionRepository.save(collection);
        log.info("Pergunta adicionada com sucesso à coleção com ID: '{}'", collectionId);
    }

    @Transactional(rollbackOn = Exception.class)
    public void removeArticleFromAllMyCollections(UUID articleId, UUID userId) {
        log.info("Removendo artigo com ID: '{}' de todas as coleções do usuário com ID: '{}'", articleId, userId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ObjectNotFoundException("Artigo não encontrado"));

        List<Collection> collections = collectionRepository.findAllByProfile_User_UserIdAndArticles_ArticleId(userId, articleId);
        for (Collection collection : collections) {
            collection.removeArticle(article);
            collectionRepository.save(collection);
        }
        log.info("Artigo removido de {} coleções", collections.size());
    }

    @Transactional(rollbackOn = Exception.class)
    public void removeQuestionFromAllMyCollections(UUID questionId, UUID userId) {
        log.info("Removendo pergunta com ID: '{}' de todas as coleções do usuário com ID: '{}'", questionId, userId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ObjectNotFoundException("Pergunta não encontrada"));

        List<Collection> collections = collectionRepository.findAllByProfile_User_UserIdAndQuestions_QuestionId(userId, questionId);
        for (Collection collection : collections) {
            collection.removeQuestion(question);
            collectionRepository.save(collection);
        }
        log.info("Pergunta removida de {} coleções", collections.size());
    }

    public void deleteCollectionById(UUID collectionId, UUID userProfileId) {
        log.info("Excluindo coleção com ID: '{}' solicitada pelo perfil de usuário com ID: '{}'", collectionId, userProfileId);
        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Comentário não encontrado"));

        if(!collection.getProfile().getUser().getUserId().equals(userProfileId) &&
                !user.getRole().equals(UserRole.ADMIN)){
            throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
        }

        collectionRepository.delete(collection);
        log.info("Coleção com ID: '{}' excluída com sucesso", collectionId);
    }
}
