package io.centralweb.backend.controller;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.enums.ExperienceLevel;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Qualification;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QualificationRepository;
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
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class QualificationControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private QualificationRepository qualificationRepository;
    private String token1;
    private String token2;
    private UUID profile1Id;
    private UUID qualification1Id;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        qualificationRepository.deleteAll();
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
        profile1.setName("Usuário Teste");
        profile1.setBio("Sou um programador de testes");
        profile1.setUser(user1);
        Profile profilesaved1 = profileRepository.save(profile1);
        profile1Id = profilesaved1.getProfileId();

        Profile profile2 = new Profile();
        profile2.setName("Usuário Teste");
        profile2.setBio("Sou um programador web");
        profile2.setUser(user2);
        profileRepository.save(profile2);

        Qualification qualification1 = new Qualification();
        qualification1.setJobTitle("Desenvolvedor Backend");
        qualification1.setExperienceLevel(ExperienceLevel.JUNIOR);
        qualification1.setInstitution("Itaú");
        qualification1.setStartDate(LocalDate.of(2021, 4, 23));
        qualification1.setEndDate(LocalDate.of(2022, 6, 17));
        qualification1.setProfile(profile1);
        Qualification qualificationSaved1 = qualificationRepository.save(qualification1);
        qualification1Id = qualificationSaved1.getQualificationId();

        Qualification qualification2 = new Qualification();
        qualification2.setJobTitle("Desenvolvedor Backend");
        qualification2.setExperienceLevel(ExperienceLevel.JUNIOR);
        qualification2.setInstitution("Americanas");
        qualification2.setStartDate(LocalDate.of(2022, 8, 11));
        qualification2.setEndDate(LocalDate.of(2023, 9, 8));
        qualification2.setProfile(profile1);
        qualificationRepository.save(qualification2);

        Qualification qualification3 = new Qualification();
        qualification3.setJobTitle("Desenvolvedor Frontend");
        qualification3.setExperienceLevel(ExperienceLevel.JUNIOR);
        qualification3.setInstitution("ICMBio");
        qualification3.setStartDate(LocalDate.of(2022, 8, 15));
        qualification3.setEndDate(LocalDate.of(2024, 9, 1));
        qualification3.setVerified(true);
        qualification3.setProfile(profile2);
        qualificationRepository.save(qualification3);

        token1 = tokenService.generateToken(user1);
        token2 = tokenService.generateToken(user2);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateQualification() {
        QualificationCreateDTO qualification = new QualificationCreateDTO(
                "Engenheiro de Software",
                ExperienceLevel.MID,
                "Mercado Livre",
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2024, 12, 6)
        );

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
                .body(qualification)
        .when()
                .post("/qualifications")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("jobTitle", equalTo("Engenheiro de Software"))
                .body("experienceLevel", equalTo("PL"))
                .body("institution", equalTo("Mercado Livre"))
                .body("startDate", equalTo(LocalDate.of(2023, 10, 1).toString()))
                .body("endDate", equalTo(LocalDate.of(2024, 12, 6).toString()));
    }

    @Test
    public void shouldReturnAllVerifiedQualifications() {
        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    public void shouldReturnAllNotVerifiedQualifications() {
        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/not-verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2));
    }

    @Test
    public void shouldReturnAllProfileVerifiedQualifications() {
        UUID profileId = profile1Id;

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/" + profileId + "/verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(0));
    }

    @Test
    public void shouldReturnAllProfileNotVerifiedQualifications() {
        UUID profileId = profile1Id;

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
                .when()
                .get("/qualifications/" + profileId + "/not-verified")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2));
    }

    @Test
    public void shouldDeleteQualification() {
        UUID qualificationId = qualification1Id;

        given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotCreateQualificationNotBeingAuthenticated() {
        QualificationCreateDTO qualification = new QualificationCreateDTO(
                "Engenheiro de Software",
                ExperienceLevel.MID,
                "Mercado Livre",
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2024, 12, 6)
        );

        given()
                .contentType(ContentType.JSON)
                .body(qualification)
        .when()
                .post("/qualifications")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotCreateQualificationWithMissingData(){
        QualificationCreateDTO qualification = new QualificationCreateDTO(
                "Engenheiro de Software",
                null,
                "",
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2024, 12, 6)
        );

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
                .body(qualification)
         .when()
                .post("/qualifications")
         .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotDeleteQualificationNotBeingAuthenticated(){
        UUID qualificationId = qualification1Id;

        given()
                .contentType(ContentType.JSON)
        .when()
                .delete("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteQuestionNonExistent(){
        UUID qualificationId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + token2)
                .contentType(ContentType.JSON)
        .when()
                .delete("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
