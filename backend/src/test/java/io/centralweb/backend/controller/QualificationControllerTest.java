package io.centralweb.backend.controller;

import io.centralweb.backend.dto.qualification.QualificationCreateDTO;
import io.centralweb.backend.enums.ExperienceLevel;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Qualification;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.QualificationRepository;
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

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class QualificationControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private QualificationRepository qualificationRepository;
    private String tokenAdmin;
    private String tokenPerson1;
    private String tokenPerson2;
    private UUID profile1Id;
    private UUID profile2Id;
    private UUID qualification1Id;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        qualificationRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("testcentraldevadmin@gmail.com");
        user1.setPassword("password");
        user1.setRole(UserRole.ADMIN);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("testcentraldev@gmail.com");
        user2.setPassword("password");
        user2.setRole(UserRole.PERSON);

        User user3 = new User();
        user3.setEmail("testcentralweb@gmail.com");
        user3.setPassword("password");
        user3.setRole(UserRole.PERSON);

        Profile profile1 = new Profile();
        profile1.setName("Usuário Teste");
        profile1.setBio("Sou um programador de testes");
        profile1.setUser(user2);
        Profile profilesaved1 = profileRepository.save(profile1);
        profile1Id = profilesaved1.getProfileId();

        Profile profile2 = new Profile();
        profile2.setName("Usuário Teste");
        profile2.setBio("Sou um programador web");
        profile2.setUser(user3);
        Profile profileSaved2 = profileRepository.save(profile2);
        profile2Id = profileSaved2.getProfileId();

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

        tokenAdmin = tokenService.generateToken(user1);
        tokenPerson1 = tokenService.generateToken(user2);
        tokenPerson2 = tokenService.generateToken(user3);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateQualificationWhenUserIsPerson() {
        QualificationCreateDTO qualification = new QualificationCreateDTO(
                "Engenheiro de Software",
                ExperienceLevel.MID,
                "Mercado Livre",
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2024, 12, 6)
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
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
    public void shouldReturnVerifiedQualificationsWhenUserIsAdmin() {
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1));
    }

    @Test
    public void shouldReturnNotVerifiedQualificationsWhenUserIsAdmin() {
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/not-verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(2));
    }

    @Test
    public void shouldReturnProfileVerifiedQualificationsWhenUserIsAdmin() {
        UUID profileId = profile2Id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/" + profileId + "/verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1));
    }

    @Test
    public void shouldReturnProfileNotVerifiedQualificationsWhenUserIsAdmin() {
        UUID profileId = profile1Id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
        .when()
                .get("/qualifications/" + profileId + "/not-verified")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(2));
    }

    @Test
    public void shouldVerifyQualificationWhenUserIsAdmin() {
        UUID qualificationId = qualification1Id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .when()
                .patch("/qualifications/" + qualificationId)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldDeleteQualificationWhenUserIsOwner() {
        UUID qualificationId = qualification1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldDeleteQualificationWhenUserIsAdmin() {
        UUID qualificationId = qualification1Id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
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
    public void shouldNotCreateQualificationWhenUserIsAdmin() {
        QualificationCreateDTO qualification = new QualificationCreateDTO(
                "Engenheiro de Software",
                ExperienceLevel.MID,
                "Mercado Livre",
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2024, 12, 6)
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(qualification)
        .when()
                .post("/qualifications")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(qualification)
         .when()
                .post("/qualifications")
         .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotVerifyQualificationWhenUserIsNotAdmin() {
        UUID qualificationId = qualification1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .patch("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotDeleteQualificationWhenUserIsNotAuthenticated(){
        UUID qualificationId = qualification1Id;

        given()
        .when()
                .delete("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteQuestionWhenDoesNotExist(){
        UUID qualificationId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .when()
                .delete("/qualifications/" + qualificationId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotDeleteQualificationWhenUserIsNotOwnerOrIsNotAdmin(){
        UUID qualificationId = qualification1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .delete("/qualifications/" + qualificationId)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
