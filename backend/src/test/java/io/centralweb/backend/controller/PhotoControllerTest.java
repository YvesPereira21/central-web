package io.centralweb.backend.controller;

import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.TokenService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PhotoControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;

    private String tokenPerson1;
    private UUID profile1Id;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        profileRepository.deleteAll();
        userRepository.deleteAll();

        User user2 = new User();
        user2.setEmail("person1@gmail.com");
        user2.setPassword("password");
        user2.setRole(UserRole.PERSON);

        Profile profile1 = new Profile();
        profile1.setName("User 1");
        profile1.setUser(user2);
        Profile savedProfile = profileRepository.save(profile1);
        profile1Id = savedProfile.getProfileId();

        tokenPerson1 = tokenService.generateToken(user2);
    }

    // ----------------------- HAPPY PATH ------------------------------

    @Test
    public void shouldUploadAvatar() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .multiPart("file", "avatar.jpg", "image bytes".getBytes(), "image/jpeg")
        .when()
                .post("/photos/" + profile1Id + "/avatar")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    // ----------------------- UNHAPPY PATH ------------------------------

    @Test
    public void shouldReturnNotFoundWhenProfileDoesNotExist() {
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .multiPart("file", "avatar.jpg", "image bytes".getBytes(), "image/jpeg")
        .when()
                .post("/photos/" + UUID.randomUUID() + "/avatar")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
