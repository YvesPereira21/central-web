package io.centralweb.backend.controller;

import io.centralweb.backend.dto.answer.AnswerCreateDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AnswerControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    private UUID question1Id;
    private UUID answerId1;
    private String token1;
    private String token2;

    @BeforeEach
    public void setUp(){
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        answerRepository.deleteAll();
        questionRepository.deleteAll();
        tagRepository.deleteAll();
        profileRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("testcentraldev@gmail.com");
        user1.setPassword("password");
        user1.setRole(UserRole.PERSON);

        User user2 = new User();
        user2.setEmail("testcentralweb@gmail.com");
        user2.setPassword("password");
        user2.setRole(UserRole.PERSON);

        Profile profile1 = new Profile();
        profile1.setName("Usuário Teste1");
        profile1.setBio("Sou um programador de testes");
        profile1.setUser(user1);
        profileRepository.save(profile1);

        Profile profile2 = new Profile();
        profile2.setName("Usuário Teste2");
        profile2.setBio("Sou um QA em desenvolvimento");
        profile2.setUser(user2);
        profileRepository.save(profile2);

        Tag tagGo = new Tag();
        tagGo.setTechnologyName("Go");
        tagGo.setColor("#ED8B00");
        tagRepository.save(tagGo);

        Tag tagRust = new Tag();
        tagRust.setTechnologyName("Rust");
        tagRust.setColor("#6DB33A");
        tagRepository.save(tagRust);

        Question question1 = new Question();
        question1.setTitle("Por que Rust é mais rápido que Go?");
        question1.setContent("Content 1");
        question1.setCreatedAt(LocalDate.now());
        question1.setPublished(true);
        question1.setTags(List.of(tagRust, tagGo));
        question1.setProfile(profile1);
        Question questionSaved1 = questionRepository.save(question1);
        question1Id = questionSaved1.getQuestionId();

        Answer answer1 = new Answer();
        answer1.setContent("Simplesmente por que Rust opera muito mais em baixo nível");
        answer1.setCreatedAt(LocalDate.now());
        answer1.setQuestion(question1);
        answer1.setProfile(profile2);
        Answer answerSaved1 = answerRepository.save(answer1);
        answerId1 = answerSaved1.getAnswerId();

        token1 = tokenService.generateToken(user1);
        token2 = tokenService.generateToken(user2);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateAnswer(){
        UUID questionId = question1Id;

        AnswerCreateDTO answer = new AnswerCreateDTO(
                "Content response",
                questionId
        );

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
                .body(answer)
        .when()
                .post("/answers")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("content", equalTo("Content response"))
        ;
    }

    @Test
    public void shouldGetAllAnswersFromQuestion(){
        UUID questionId = question1Id;

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .get("/answers/" + questionId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    public void shouldAcceptAnswer(){
        UUID answerId = answerId1;

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
                .body(answerId)
        .when()
                .patch("/answers/" + answerId)
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldDeleteAnswer(){
        UUID answerId = answerId1;

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
        .when()
                .delete("/answers/" + answerId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotCreateAnswerNotBeingAuthenticated(){
        UUID questionId = question1Id;

        AnswerCreateDTO answer = new AnswerCreateDTO(
                "Content response",
                questionId
        );

        given()
                .contentType(ContentType.JSON)
                .body(answer)
        .when()
                .post("/answers")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotCreateAnswerWithMissingData(){
        AnswerCreateDTO answer = new AnswerCreateDTO(
                "Content response",
                null
        );

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
                .body(answer)
        .when()
                .post("/answers")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateAnswerWithNonExistentQuestion(){
        UUID questionId = UUID.randomUUID();
        AnswerCreateDTO answer = new AnswerCreateDTO(
                "Content response",
                questionId
        );

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
                .body(answer)
        .when()
                .post("/answers")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotAcceptAnswerNonExistent(){
        UUID answerId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .patch("/answers/" + answerId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotAcceptAnswerFromQuestionWhereProfileIsNotOwner(){
        UUID answerId = answerId1;

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
                .body(answerId)
        .when()
                .patch("/answers")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotDeleteAnswerNotBeingAuthenticated(){
        UUID answerId = answerId1;

        given()
                .contentType(ContentType.JSON)
        .when()
                .delete("/answers/" + answerId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteAnswerNonExistent(){
        UUID answerId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
        .when()
                .delete("/answers/" + answerId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
