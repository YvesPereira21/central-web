package io.centralweb.backend.controller;

import io.centralweb.backend.dto.profile.ProfileCreateDTO;
import io.centralweb.backend.dto.profile.ProfileUpdateDTO;
import io.centralweb.backend.dto.user.UserDTO;
import io.centralweb.backend.enums.ProfileType;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
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

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProfileControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProfileRepository profileRepository;
    private String token1;
    private String token2;
    private UUID profile1Id;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

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
        profile1.setProfileType(ProfileType.UNDERGRADUATE);
        profile1.setExpertise("Desenvolvedor Fullstack");
        profile1.setLevel("Especialista");
        profile1.setReputationScore(1200);
        profile1.setUser(user1);
        Profile profileSaved1 = profileRepository.save(profile1);
        profile1Id = profileSaved1.getProfileId();

        Profile profile2 = new Profile();
        profile2.setName("Usuário Bom");
        profile2.setBio("Sou um desenvolvedor em Segurança da Informação");
        profile2.setProfileType(ProfileType.PROFESSIONAL);
        profile2.setExpertise("Analista SOC");
        profile2.setLevel("Especialista");
        profile2.setReputationScore(2760);
        profile2.setProfessional(true);
        profile2.setUser(user2);
        profileRepository.save(profile2);

        token1 = tokenService.generateToken(user1);
        token2 = tokenService.generateToken(user2);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateProfile(){
        UserDTO user = new UserDTO(
                "testcentraljunior@gmail.com",
                "password"
        );

        ProfileCreateDTO profile = new ProfileCreateDTO(
                "Usuário Teste",
                "Sou um programador de QA",
                ProfileType.SELFTAUGHT,
                "Aspirante a tecnologia",
                user
        );

        given()
                .contentType(ContentType.JSON)
                .body(profile)
        .when()
                .post("/profiles")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("Usuário Teste"))
                .body("bio", equalTo("Sou um programador de QA"))
                .body("expertise", equalTo("Aspirante a tecnologia"))
                .body("level", equalTo("Iniciante"))
                .body("reputationScore", equalTo(0))
                .body("professional", equalTo(false));
    }

    @Test
    public void shouldReturnProfile(){
        UUID profileId = profile1Id;

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .get("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("profileId", equalTo(profileId.toString()))
                .body("name", equalTo("Usuário Teste1"))
                .body("bio", equalTo("Sou um programador de testes"))
                .body("expertise", equalTo("Desenvolvedor Fullstack"))
                .body("level", equalTo("Especialista"))
                .body("reputationScore", equalTo(1200))
                .body("professional", equalTo(false));
    }

    @Test
    public void shouldUpdateProfile(){
        UUID profileId = profile1Id;
        ProfileUpdateDTO profileUpdated = new ProfileUpdateDTO(
                "Galego",
                "Melhor programador Django",
                "Desenvolvedor Fullstack"
        );

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
                .body(profileUpdated)
        .when()
                .put("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("Galego"))
                .body("bio", equalTo("Melhor programador Django"))
                .body("expertise", equalTo("Desenvolvedor Fullstack"));
    }

    @Test
    public void shouldDeleteProfile(){
        UUID profileId = profile1Id;

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotCreateProfileWithMissingData(){
        UserDTO user = new UserDTO(
                "testcentraljunior@gmail.com",
                ""
        );

        ProfileCreateDTO profile = new ProfileCreateDTO(
                "Galego",
                "Sou um programador de QA",
                ProfileType.SELFTAUGHT,
                "QA",
                user
        );

        given()
                .contentType(ContentType.JSON)
                .body(profile)
        .when()
                .post("/profiles")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateProfileWithEmailAlreadyRegistered(){
        UserDTO user = new UserDTO(
                "testcentraldev@gmail.com",
                "password"
        );

        ProfileCreateDTO profile = new ProfileCreateDTO(
                "Renato",
                "Sou um programador de QA",
                ProfileType.SELFTAUGHT,
                "QA",
                user
        );

        given()
                .contentType(ContentType.JSON)
                .body(profile)
        .when()
                .post("/profiles")
        .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldNotCreateProfileWithInvalidEmail(){
        UserDTO user = new UserDTO(
                "test.htmail@com",
                "password"
        );

        ProfileCreateDTO profile = new ProfileCreateDTO(
                "Renato",
                "Sou um programador de QA",
                ProfileType.SELFTAUGHT,
                "QA",
                user
        );

        given()
                .contentType(ContentType.JSON)
                .body(profile)
        .when()
                .post("/profiles")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotUpdateProfileNotBeingAuthenticated(){
        UUID profileId = profile1Id;
        ProfileUpdateDTO profileUpdated = new ProfileUpdateDTO(
                "Galego",
                "Melhor programador Django",
                "Desenvolvedor Fullstack"
        );

        given()
                .contentType(ContentType.JSON)
                .body(profileUpdated)
        .when()
                .put("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotUpdateProfileWhereProfileIsNotOwner(){
        UUID profileId = profile1Id;
        ProfileUpdateDTO profileUpdated = new ProfileUpdateDTO(
                "Galego",
                "Melhor programador Django",
                "Desenvolvedor Fullstack"
        );

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
                .body(profileUpdated)
        .when()
                .put("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotDeleteProfileNotBeingAuthenticated(){
        UUID profileId = profile1Id;

        given()
                .contentType(ContentType.JSON)
        .when()
                .delete("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteProfileNonExistent(){
        UUID profileId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/profiles/" + profileId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
