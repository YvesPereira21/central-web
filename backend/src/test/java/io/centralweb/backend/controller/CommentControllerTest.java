package io.centralweb.backend.controller;

import io.centralweb.backend.dto.comment.CommentCreateDTO;
import io.centralweb.backend.dto.comment.CommentUpdateDTO;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.model.Answer;
import io.centralweb.backend.model.Comment;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.AnswerRepository;
import io.centralweb.backend.repository.CommentRepository;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
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
public class CommentControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private CommentRepository commentRepository;

    private String tokenAdmin;
    private String tokenPerson1;
    private String tokenPerson2;
    private UUID answerId;
    private UUID comment1Id;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        commentRepository.deleteAll();
        answerRepository.deleteAll();
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

        Answer answer1 = new Answer();
        answer1.setContent("This is an answer");
        answer1.setCreatedAt(LocalDate.now());
        answer1.setProfile(profile1);
        Answer savedAnswer = answerRepository.save(answer1);
        answerId = savedAnswer.getAnswerId();

        Comment comment1 = new Comment();
        comment1.setContent("This is a comment");
        comment1.setCreatedAt(LocalDate.now());
        comment1.setAnswer(savedAnswer);
        comment1.setProfile(profile1);
        Comment savedComment = commentRepository.save(comment1);
        comment1Id = savedComment.getCommentId();

        tokenAdmin = tokenService.generateToken(user1);
        tokenPerson1 = tokenService.generateToken(user2);
        tokenPerson2 = tokenService.generateToken(user3);
    }

    // ----------------------- HAPPY PATH ------------------------------

    @Test
    public void shouldCreateCommentWhenUserIsPerson() {
        CommentCreateDTO payload = new CommentCreateDTO("Novo comentário de teste");

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/comments/answer/" + answerId)
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("content", equalTo("Novo comentário de teste"));
    }

    @Test
    public void shouldReturnCommentsByAnswer() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/comments/answer/" + answerId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1))
                .body("content[0].content", equalTo("This is a comment"));
    }

    @Test
    public void shouldUpdateCommentWhenUserIsOwner() {
        CommentUpdateDTO payload = new CommentUpdateDTO("Comentário atualizado");

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .put("/comments/" + comment1Id)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", equalTo("Comentário atualizado"));
    }

    @Test
    public void shouldDeleteCommentWhenUserIsOwner() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
        .when()
                .delete("/comments/" + comment1Id)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldDeleteCommentWhenUserIsAdmin() {
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .delete("/comments/" + comment1Id)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // ----------------------- UNHAPPY PATH ------------------------------

    @Test
    public void shouldNotCreateCommentWhenUserIsNotAuthenticated() {
        CommentCreateDTO payload = new CommentCreateDTO("Teste");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/comments/answer/" + answerId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotCreateCommentWhenAnswerDoesNotExist() {
        CommentCreateDTO payload = new CommentCreateDTO("Teste");

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/comments/answer/" + UUID.randomUUID())
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotUpdateCommentWhenUserIsNotOwner() {
        CommentUpdateDTO payload = new CommentUpdateDTO("Comentário atualizado por intruso");

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .put("/comments/" + comment1Id)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotDeleteCommentWhenUserIsNotOwnerOrAdmin() {
        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .delete("/comments/" + comment1Id)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
