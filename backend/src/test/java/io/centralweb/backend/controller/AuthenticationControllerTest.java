package io.centralweb.backend.controller;

import io.centralweb.backend.dto.user.*;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthenticationControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(){
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        userRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("testcentraldev@gmail.com");
        user1.setPassword(bCryptPasswordEncoder.encode("password"));
        user1.setRole(UserRole.PERSON);
        userRepository.save(user1);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldGenerateToken(){
        LoginRequestDTO login = new LoginRequestDTO(
                "testcentraldev@gmail.com",
                "password"
        );

        given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotGenerateTokenWithMissingData(){
        LoginRequestDTO login = new LoginRequestDTO(
                "testcentraldev@gmail.com",
                ""
        );

        given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotGenerateTokenWithInvalidData(){
        LoginRequestDTO login = new LoginRequestDTO(
                "testcentral@gmail.com",
                "password"
        );

        given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldRefreshTokenWhenTokenIsValid(){
        LoginRequestDTO login = new LoginRequestDTO(
                "testcentraldev@gmail.com",
                "password"
        );

        // 1. Login to get refresh token
        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("refreshToken");

        // 2. Use refresh token to get a new access token and rotated refresh token
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
        .when()
                .post("/auth/refresh")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("refreshToken", not(equalTo(refreshToken))); // Verify token rotation
    }

    @Test
    public void shouldNotRefreshTokenWhenTokenDoesNotExist(){
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO("non-existent-token");

        given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
        .when()
                .post("/auth/refresh")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldLogoutSuccessfully(){
        LoginRequestDTO login = new LoginRequestDTO(
                "testcentraldev@gmail.com",
                "password"
        );

        // 1. Login to get refresh token
        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("refreshToken");

        // 2. Perform logout
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(logoutRequest)
        .when()
                .post("/auth/logout")
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 3. Try to refresh with the logged out token - should fail because it was deleted
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
        .when()
                .post("/auth/refresh")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
