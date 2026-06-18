package io.centralweb.backend.controller;

import io.centralweb.backend.dto.collection.CollectionCreateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.model.*;
import io.centralweb.backend.repository.*;
import io.centralweb.backend.security.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CollectionControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private CollectionRepository collectionRepository;

    private String tokenAdmin;
    private String tokenPerson1;
    private String tokenPerson2;
    private UUID collection1Id;
    private UUID article1Id;
    private UUID question1Id;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        collectionRepository.deleteAll();
        articleRepository.deleteAll();
        questionRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("admin@gmail.com");
        user1.setPassword("password");
        user1.setRole(UserRole.ADMIN);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("person1@gmail.com");
        user2.setPassword("password");
        user2.setRole(UserRole.PERSON);

        User user3 = new User();
        user3.setEmail("person2@gmail.com");
        user3.setPassword("password");
        user3.setRole(UserRole.PERSON);

        Profile profile1 = new Profile();
        profile1.setName("User 1");
        profile1.setUser(user2);
        profileRepository.save(profile1);

        Profile profile2 = new Profile();
        profile2.setName("User 2");
        profile2.setUser(user3);
        profileRepository.save(profile2);

        Article article1 = new Article();
        article1.setTitle("Title");
        article1.setContent("Content");
        article1.setCreatedAt(LocalDate.now());
        article1.setProfile(profile1);
        Article savedArticle = articleRepository.save(article1);
        article1Id = savedArticle.getArticleId();

        Question question1 = new Question();
        question1.setTitle("Question");
        question1.setContent("Content");
        question1.setCreatedAt(LocalDate.now());
        question1.setProfile(profile1);
        Question savedQuestion = questionRepository.save(question1);
        question1Id = savedQuestion.getQuestionId();

        Collection collection1 = new Collection();
        collection1.setName("Favoritos");
        collection1.setProfile(profile1);
        collection1.addArticle(savedArticle);
        collection1.addQuestion(savedQuestion);
        Collection savedCollection = collectionRepository.save(collection1);
        collection1Id = savedCollection.getCollectionId();

        tokenAdmin = tokenService.generateToken(user1);
        tokenPerson1 = tokenService.generateToken(user2);
        tokenPerson2 = tokenService.generateToken(user3);
    }

    // ----------------------- HAPPY PATH ------------------------------

    @Test
    public void shouldCreateCollectionWhenUserIsPerson() {
        CollectionCreateDTO payload = new CollectionCreateDTO("Nova Coleção");

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/collections")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("Nova Coleção"));
    }

    @Test
    public void shouldReturnMyCollections() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/collections/my-collections")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1))
                .body("content[0].name", equalTo("Favoritos"));
    }

    @Test
    public void shouldReturnCollectionById() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/collections/" + collection1Id)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("Favoritos"));
    }

    @Test
    public void shouldAddArticleToCollection() {
        // We create a new article to add to the existing collection
        Article article2 = new Article();
        article2.setTitle("Title 2");
        article2.setContent("Content 2");
        article2.setCreatedAt(LocalDate.now());
        // Just setting directly to pass not null
        article2.setProfile(profileRepository.findAll().get(0));
        Article savedArticle2 = articleRepository.save(article2);

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .post("/collections/" + collection1Id + "/articles/" + savedArticle2.getArticleId())
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldAddQuestionToCollection() {
        // We create a new question to add to the existing collection
        Question question2 = new Question();
        question2.setTitle("Question 2");
        question2.setContent("Content 2");
        question2.setCreatedAt(LocalDate.now());
        question2.setProfile(profileRepository.findAll().get(0));
        Question savedQuestion2 = questionRepository.save(question2);

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .post("/collections/" + collection1Id + "/questions/" + savedQuestion2.getQuestionId())
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldRemoveArticleFromAllMyCollections() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/collections/articles/" + article1Id)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldRemoveQuestionFromAllMyCollections() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/collections/questions/" + question1Id)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldDeleteCollectionWhenUserIsOwner() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
        .when()
                .delete("/collections/" + collection1Id)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldDeleteCollectionWhenUserIsAdmin() {
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .delete("/collections/" + collection1Id)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // ----------------------- UNHAPPY PATH ------------------------------

    @Test
    public void shouldNotCreateCollectionWhenUserIsNotAuthenticated() {
        CollectionCreateDTO payload = new CollectionCreateDTO("Nova Coleção");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/collections")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotGetCollectionByIdWhenNotOwner() {
        given()
                .header("Authorization", "Bearer " + tokenPerson2)
                .contentType(ContentType.JSON)
        .when()
                .get("/collections/" + collection1Id)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotAddArticleToCollectionWhenNotOwner() {
        given()
                .header("Authorization", "Bearer " + tokenPerson2)
                .contentType(ContentType.JSON)
        .when()
                .post("/collections/" + collection1Id + "/articles/" + article1Id)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotDeleteCollectionWhenUserIsNotOwnerOrAdmin() {
        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .delete("/collections/" + collection1Id)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
