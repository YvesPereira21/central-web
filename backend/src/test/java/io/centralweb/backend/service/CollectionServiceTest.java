package io.centralweb.backend.service;

import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.dto.collection.CollectionDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.exception.ObjectNotFoundException;
import io.centralweb.backend.exception.ProfileIsNotTheOwnerException;
import io.centralweb.backend.dto.mapper.CollectionMapper;
import io.centralweb.backend.model.*;
import io.centralweb.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private CollectionService collectionService;

    private User userAdmin;
    private User userPerson;
    private User userPersonOther;
    private Profile profile;
    private Profile profileOther;
    private Collection collection;
    private CollectionCreateDTO createDTO;
    private CollectionDTO collectionDTO;
    private Article article;
    private Question question;

    @BeforeEach
    public void setUp() {
        userAdmin = new User();
        ReflectionTestUtils.setField(userAdmin, "userId", UUID.randomUUID());
        userAdmin.setRole(UserRole.ADMIN);

        userPerson = new User();
        ReflectionTestUtils.setField(userPerson, "userId", UUID.randomUUID());
        userPerson.setRole(UserRole.PERSON);

        userPersonOther = new User();
        ReflectionTestUtils.setField(userPersonOther, "userId", UUID.randomUUID());
        userPersonOther.setRole(UserRole.PERSON);

        profile = new Profile();
        ReflectionTestUtils.setField(profile, "profileId", UUID.randomUUID());
        profile.setName("User 1");
        profile.setUser(userPerson);

        profileOther = new Profile();
        ReflectionTestUtils.setField(profileOther, "profileId", UUID.randomUUID());
        profileOther.setName("User 2");
        profileOther.setUser(userPersonOther);

        collection = new Collection();
        ReflectionTestUtils.setField(collection, "collectionId", UUID.randomUUID());
        collection.setName("Minha Coleção");
        collection.setProfile(profile);
        collection.setArticles(new HashSet<>());
        collection.setQuestions(new HashSet<>());

        createDTO = new CollectionCreateDTO("Minha Coleção");

        ProfileSimpleDTO profileSimpleDTO = new ProfileSimpleDTO(
                profile.getProfileId(),
                userPerson.getUserId(),
                "User 1",
                "Iniciante",
                false,
                null
        );

        collectionDTO = new CollectionDTO(
                collection.getCollectionId(),
                collection.getName(),
                Set.of(),
                Set.of()
        );

        article = new Article();
        ReflectionTestUtils.setField(article, "articleId", UUID.randomUUID());

        question = new Question();
        ReflectionTestUtils.setField(question, "questionId", UUID.randomUUID());
    }

    // ----------------------- HAPPY PATH ------------------------------

    @Test
    void shouldCreateCollection() {
        UUID userId = userPerson.getUserId();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);
        when(collectionMapper.toDTO(any(Collection.class))).thenReturn(collectionDTO);

        CollectionDTO result = collectionService.createCollection(createDTO, userId);

        assertNotNull(result);
        assertEquals("Minha Coleção", result.name());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(collectionRepository, times(1)).save(any(Collection.class));
        verify(collectionMapper, times(1)).toDTO(any(Collection.class));
    }

    @Test
    void shouldGetMyCollections() {
        UUID userId = userPerson.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Collection> collectionPage = new PageImpl<>(List.of(collection));

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(collectionRepository.findAllByProfile_ProfileId(profile.getProfileId(), pageable)).thenReturn(collectionPage);
        when(collectionMapper.toDTO(any(Collection.class))).thenReturn(collectionDTO);

        Page<CollectionDTO> result = collectionService.getMyCollections(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());

        verify(profileRepository, times(1)).findByUser_UserId(userId);
        verify(collectionRepository, times(1)).findAllByProfile_ProfileId(profile.getProfileId(), pageable);
    }

    @Test
    void shouldGetCollectionById() {
        UUID collectionId = collection.getCollectionId();
        UUID userId = userPerson.getUserId();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(collectionMapper.toDTO(collection)).thenReturn(collectionDTO);

        CollectionDTO result = collectionService.getCollectionById(collectionId, userId);

        assertNotNull(result);
        assertEquals(collectionId, result.collectionId());

        verify(collectionRepository, times(1)).findById(collectionId);
    }

    @Test
    void shouldAddArticleToCollection() {
        UUID collectionId = collection.getCollectionId();
        UUID articleId = article.getArticleId();
        UUID userId = userPerson.getUserId();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        assertDoesNotThrow(() -> collectionService.addArticleToCollection(collectionId, articleId, userId));

        assertTrue(collection.getArticles().contains(article));
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void shouldAddQuestionToCollection() {
        UUID collectionId = collection.getCollectionId();
        UUID questionId = question.getQuestionId();
        UUID userId = userPerson.getUserId();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        assertDoesNotThrow(() -> collectionService.addQuestionToCollection(collectionId, questionId, userId));

        assertTrue(collection.getQuestions().contains(question));
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void shouldRemoveArticleFromAllMyCollections() {
        UUID articleId = article.getArticleId();
        UUID userId = userPerson.getUserId();

        collection.addArticle(article);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(collectionRepository.findAllByProfile_User_UserIdAndArticles_ArticleId(userId, articleId))
                .thenReturn(List.of(collection));

        assertDoesNotThrow(() -> collectionService.removeArticleFromAllMyCollections(articleId, userId));

        assertFalse(collection.getArticles().contains(article));
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void shouldRemoveQuestionFromAllMyCollections() {
        UUID questionId = question.getQuestionId();
        UUID userId = userPerson.getUserId();

        collection.addQuestion(question);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(collectionRepository.findAllByProfile_User_UserIdAndQuestions_QuestionId(userId, questionId))
                .thenReturn(List.of(collection));

        assertDoesNotThrow(() -> collectionService.removeQuestionFromAllMyCollections(questionId, userId));

        assertFalse(collection.getQuestions().contains(question));
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void shouldDeleteCollectionWhenUserIsOwner() {
        UUID collectionId = collection.getCollectionId();
        UUID userId = userPerson.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPerson));
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        assertDoesNotThrow(() -> collectionService.deleteCollectionById(collectionId, userId));

        verify(collectionRepository, times(1)).delete(collection);
    }

    @Test
    void shouldDeleteCollectionWhenUserIsAdmin() {
        UUID collectionId = collection.getCollectionId();
        UUID userId = userAdmin.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userAdmin));
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        assertDoesNotThrow(() -> collectionService.deleteCollectionById(collectionId, userId));

        verify(collectionRepository, times(1)).delete(collection);
    }

    // ----------------------- UNHAPPY PATH ------------------------------

    @Test
    void shouldNotCreateCollectionAndThrowExceptionWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> collectionService.createCollection(createDTO, userId));

        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void shouldNotGetCollectionByIdAndThrowExceptionWhenNotOwner() {
        UUID collectionId = collection.getCollectionId();
        UUID userId = userPersonOther.getUserId();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        assertThrows(ProfileIsNotTheOwnerException.class, () -> collectionService.getCollectionById(collectionId, userId));
    }

    @Test
    void shouldNotAddArticleToCollectionWhenCollectionNotFound() {
        UUID collectionId = UUID.randomUUID();
        UUID articleId = article.getArticleId();
        UUID userId = userPerson.getUserId();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> collectionService.addArticleToCollection(collectionId, articleId, userId));
    }

    @Test
    void shouldNotAddArticleToCollectionWhenNotOwner() {
        UUID collectionId = collection.getCollectionId();
        UUID articleId = article.getArticleId();
        UUID userId = userPersonOther.getUserId();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        assertThrows(ProfileIsNotTheOwnerException.class, () -> collectionService.addArticleToCollection(collectionId, articleId, userId));
    }

    @Test
    void shouldNotDeleteCollectionWhenNotOwnerOrAdmin() {
        UUID collectionId = collection.getCollectionId();
        UUID userId = userPersonOther.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userPersonOther));
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        assertThrows(ProfileIsNotTheOwnerException.class, () -> collectionService.deleteCollectionById(collectionId, userId));
    }
}
