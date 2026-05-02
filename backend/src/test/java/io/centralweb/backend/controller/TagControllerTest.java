package io.centralweb.backend.controller;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.tag.TagUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.TagRepository;
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

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TagControllerTest {
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
    private String tokenAdmin;
    private String tokenPerson;
    private UUID tag1Id;
    private String technologyName;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        User user1 = new User();
        user1.setEmail("testcentraldev@gmail.com");
        user1.setPassword("password");
        user1.setRole(UserRole.ADMIN);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("testcentralweb@gmail.com");
        user2.setPassword("password");
        user2.setRole(UserRole.PERSON);

        Profile profile1 = new Profile();
        profile1.setName("Usuário Teste1");
        profile1.setBio("Sou um programador de testes");
        profile1.setUser(user2);
        profileRepository.save(profile1);

        Tag tagJava = new Tag();
        tagJava.setTechnologyName("Java");
        tagJava.setColor("#ED8B00");
        Tag tagSaved = tagRepository.save(tagJava);
        tag1Id = tagSaved.getTagId();
        technologyName = tagSaved.getTechnologyName();

        Tag tagPython = new Tag();
        tagPython.setTechnologyName("Python");
        tagPython.setColor("#6DB33A");
        tagRepository.save(tagPython);

        tokenAdmin = tokenService.generateToken(user1);
        tokenPerson = tokenService.generateToken(user2);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateTagWhenUserIsAdmin() {
        TagDTO tagRuby = new TagDTO(
                "Ruby",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(tagRuby)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("technologyName", equalTo("Ruby"))
                .body("color", equalTo("#6DB33A"));
    }

    @Test
    public void shouldReturnTagsRegistered(){
        given()
                .header("Authorization", "Bearer " + tokenPerson)
                .contentType(ContentType.JSON)
        .when()
                .get("/tags")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(2));
    }

    @Test
    public void shouldUpdateTagWhenUserIsAdmin() {
        UUID tagId = tag1Id;
        TagUpdateDTO tagSwift = new TagUpdateDTO(
                "Swift",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(tagSwift)
        .when()
                .put("/tags/" + tagId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("technologyName", equalTo("Swift"))
                .body("color", equalTo("#6DB33A"));
    }

    @Test
    public void shouldDeleteTagWhenUserIsAdmin() {
        String technName = technologyName;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .delete("/tags/" + technName)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotCreateTagWhenUserIsNotAuthenticated(){
        TagDTO tagRuby = new TagDTO(
                "Ruby",
                "#6DB33A"
        );

        given()
                .contentType(ContentType.JSON)
                .body(tagRuby)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotCreateTagWhenUserIsNotAdmin(){
        TagDTO tagRuby = new TagDTO(
                "Ruby",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson)
                .contentType(ContentType.JSON)
                .body(tagRuby)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotCreateTagWithMissingData(){
        TagDTO tagJava = new TagDTO(
                "",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(tagJava)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateTagWhenTechnologyNameAlreadyExist(){
        TagDTO tagJava = new TagDTO(
                "Java",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(tagJava)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldNotUpdateTagWhenUserIsNotAuthenticated(){
        UUID tagId = tag1Id;
        TagUpdateDTO tagPerl = new TagUpdateDTO(
                "Perl",
                "#6DB33A"
        );

        given()
                .contentType(ContentType.JSON)
                .body(tagPerl)
        .when()
                .put("/tags/" + tagId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotUpdateTagWhenUserIsNotAdmin(){
        UUID tagId = tag1Id;
        TagUpdateDTO tagPerl = new TagUpdateDTO(
                "Perl",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson)
                .contentType(ContentType.JSON)
                .body(tagPerl)
        .when()
                .put("/tags/" + tagId)
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotUpdateTagWhenDoesNotExist(){
        UUID tagId = UUID.randomUUID();
        TagUpdateDTO tagErlang = new TagUpdateDTO(
                "Erlang",
                "#6DB33A"
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(tagErlang)
        .when()
                .put("/tags/" + tagId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotDeleteTagWhenUserIsNotAuthenticated(){
        String techName = technologyName;

        given()
        .when()
                .delete("/tags/" + techName)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteTagWhenUserIsNotAdmin(){
        String techName = technologyName;

        given()
                .header("Authorization", "Bearer " + tokenPerson)
        .when()
                .delete("/tags/" + techName)
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotDeleteTagWhenDoesNotExist(){
        String techName = "Javascript";

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .delete("/tags/" + techName)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
