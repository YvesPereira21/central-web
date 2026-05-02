package io.centralweb.backend.controller;

import io.centralweb.backend.dto.question.QuestionCreateDTO;
import io.centralweb.backend.dto.question.QuestionUpdateDTO;
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
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class QuestionControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    private String tokenAdmin;
    private String tokenPerson1;
    private String tokenPerson2;
    private UUID question1id;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        answerRepository.deleteAll();
        questionRepository.deleteAll();
        tagRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("testcentraldev@gmail.com");
        user1.setPassword("password");
        user1.setRole(UserRole.ADMIN);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("testcentralweb@gmail.com");
        user2.setPassword("password");
        user2.setRole(UserRole.PERSON);

        User user3 = new User();
        user3.setEmail("testcentraljunior@gmail.com");
        user3.setPassword("password");
        user3.setRole(UserRole.PERSON);

        Profile profile1 = new Profile();
        profile1.setName("Usuário Teste");
        profile1.setBio("Sou um programador de testes");
        profile1.setUser(user2);
        profileRepository.save(profile1);

        Profile profile2 = new Profile();
        profile2.setName("Usuário Teste");
        profile2.setBio("Sou um programador de QA");
        profile2.setUser(user3);
        profileRepository.save(profile2);

        Tag tagJava = new Tag();
        tagJava.setTechnologyName("Java");
        tagJava.setColor("#ED8B00");
        tagRepository.save(tagJava);

        Tag tagSpring = new Tag();
        tagSpring.setTechnologyName("Spring");
        tagSpring.setColor("#6DB33F");
        tagRepository.save(tagSpring);

        Tag tagPython = new Tag();
        tagPython.setTechnologyName("Python");
        tagPython.setColor("#6DB33A");
        tagRepository.save(tagPython);

        Question question1 = new Question();
        question1.setTitle("Diferenças entre Python x Java");
        question1.setContent("Content 1");
        question1.setCreatedAt(LocalDate.now());
        question1.setPublished(true);
        question1.setTags(List.of(tagJava, tagPython));
        question1.setProfile(profile1);
        Question questionSaved1 = questionRepository.save(question1);
        question1id = questionSaved1.getQuestionId();

        Question question2 = new Question();
        question2.setTitle("Como implementar index no Spring");
        question2.setContent("Content 2");
        question2.setCreatedAt(LocalDate.now());
        question2.setPublished(true);
        question2.setTags(List.of(tagSpring));
        question2.setProfile(profile2);
        questionRepository.save(question2);

        Question question3 = new Question();
        question3.setTitle("Como implementar redis no Python");
        question3.setContent("Content 2");
        question3.setCreatedAt(LocalDate.now());
        question3.setPublished(true);
        question3.setTags(List.of(tagPython));
        question3.setProfile(profile2);
        questionRepository.save(question3);

        Answer answer1 = new Answer();
        answer1.setContent("Content Response 1");
        answer1.setCreatedAt(LocalDate.now());
        answer1.setQuestion(question1);
        answer1.setProfile(profile1);
        answerRepository.save(answer1);

        Answer answer2 = new Answer();
        answer2.setContent("Content Response 2");
        answer2.setCreatedAt(LocalDate.now());
        answer2.setAccepted(true);
        answer2.setQuestion(question1);
        answer2.setProfile(profile2);
        answerRepository.save(answer2);

        tokenAdmin = tokenService.generateToken(user1);
        tokenPerson1 = tokenService.generateToken(user2);
        tokenPerson2 = tokenService.generateToken(user3);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateQuestionWhenUserIsPerson(){
        QuestionCreateDTO question = new QuestionCreateDTO(
                "Teste de integração",
                "Conteúdo extenso do artigo para passar na validação...",
                List.of("Java", "Spring")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .post("/questions")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("title", equalTo("Teste de integração"))
                .body("content", equalTo("Conteúdo extenso do artigo para passar na validação..."))
                .body("tags", hasSize(2))
                .body("tags.technologyName", hasItems("Java", "Spring"));
    }

    @Test
    public void shouldReturnQuestion(){
        UUID questionId = question1id;

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Diferenças entre Python x Java"))
                .body("content", equalTo("Content 1"))
                .body("tags", hasSize(2))
                .body("tags.technologyName", hasItems("Java", "Python"));
    }

    @Test
    public void shouldReturnQuestionsPublished(){
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/questions")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(3));
    }

    @Test
    public void shouldReturnPublishedQuestionsWithGivenTitle(){
        String title = "implementar index";

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/questions/" + title + "/title")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1));
    }

    @Test
    public void shouldReturnAllQuestionsPublishedWithGivenTechnologyName(){
        String technologyName = "Python";

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
        .when()
                .get("/questions/" + technologyName + "/tag")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(2));
    }

    @Test
    public void shouldReturnOnlyQuestionsWithAcceptedAnswers(){
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/questions/accepteds-answers")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1));
    }

    @Test
    public void shouldUpdateQuestionWhenUserIsOwnerAndIsPerson(){
        UUID questionId = question1id;
        QuestionUpdateDTO questionUpdated = new QuestionUpdateDTO(
                "Teste de integração 123",
                "Este conteúdo foi atualizado para ver se funciona",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(questionUpdated)
        .when()
                .put("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Teste de integração 123"))
                .body("content", equalTo("Este conteúdo foi atualizado para ver se funciona"))
                .body("tags", hasSize(1))
                .body("tags.technologyName", hasItem("Python"));
    }

    @Test
    public void shouldToggleLikeWhenUserIsPerson(){
        UUID questionId = question1id;

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .patch("/questions/" + questionId + "/like")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldDeleteQuestionWhenUserIsOwner(){
        UUID questionId = question1id;

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldDeleteQuestionWhenUserIsAdmin(){
        UUID questionId = question1id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
        .when()
                .delete("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotCreateQuestionWhenUserIsNotAuthenticated(){
        QuestionCreateDTO question = new QuestionCreateDTO(
                "Como fazer partição de lista",
                "Para isso você consegue fazer na linguagem Python dessa forma...",
                List.of("Python")
        );

        given()
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .post("/questions")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotCreateQuestionWhenUserIsNotPerson(){
        QuestionCreateDTO question = new QuestionCreateDTO(
                "Como fazer partição de lista",
                "Para isso você consegue fazer na linguagem Python dessa forma...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .post("/questions")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotCreateQuestionWithMissingData(){
        QuestionCreateDTO question = new QuestionCreateDTO(
                "",
                "Para você implementar isso tem de fazer assim...",
                List.of()
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .post("/questions")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateQuestionWhenTechnologyNameDoesNotExist(){
        QuestionCreateDTO question = new QuestionCreateDTO(
                "Como fazer partição de lista",
                "Para isso você consegue fazer na linguagem Rust desse jeito...",
                List.of("Rust")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .post("/questions")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotUpdateQuestionWhenUserIsNotAuthenticated(){
        UUID questionId = question1id;
        QuestionUpdateDTO question = new QuestionUpdateDTO(
                "Frameworks Python para web",
                "Existem vários frameworks web para Python que são muito utilizados...",
                List.of("Python")
        );

        given()
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .put("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotUpdateQuestionWhenUserIsAdmin(){
        UUID questionId = question1id;
        QuestionUpdateDTO question = new QuestionUpdateDTO(
                "Frameworks Python para web",
                "Existem vários frameworks web para Python que são muito utilizados...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .put("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotUpdateQuestionWhenUserIsNotOwner(){
        UUID questionId = question1id;
        QuestionUpdateDTO question = new QuestionUpdateDTO(
                "Frameworks Python para web",
                "Existem vários frameworks web para Python que são muito utilizados...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
                .contentType(ContentType.JSON)
                .body(question)
        .when()
                .put("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotUpdateQuestionWhenItDoesNotExist(){
        UUID questionId = UUID.randomUUID();
        QuestionUpdateDTO questionUpdated = new QuestionUpdateDTO(
                "Como criar um dicionário no Python",
                "Para você criar um dicionário no python, é preciso fazer isso...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(questionUpdated)
        .when()
                .put("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotUpdateQuestionWhenTechnologyNameDoesNotExist(){
        UUID questionId = question1id;
        QuestionUpdateDTO questionUpdated = new QuestionUpdateDTO(
                "CPython usando Rust",
                "Uma notícia que bombou recentemente é o Python trocando o C pelo Rust...",
                List.of("Python", "Rust")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(questionUpdated)
        .when()
                .put("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotToggleLikeWhenQuestionDoesNotExist(){
        UUID questionId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
        .when()
                .patch("/questions/" + questionId + "/like")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotToggleLikeWhenUserIsAdmin(){
        UUID questionId = question1id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .patch("/questions/" + questionId + "/like")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotDeleteQuestionWhenUserIsNotAuthenticated(){
        UUID questionId = question1id;

        given()
        .when()
                .delete("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteQuestionWhenDoesNotExist(){
        UUID questionId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
        .when()
                .delete("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotDeleteQuestionWhenUserIsNotOwnerOrIsNotAdmin(){
        UUID questionId = question1id;

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .delete("/questions/" + questionId)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
