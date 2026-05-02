package io.centralweb.backend.controller;

import io.centralweb.backend.dto.user.LoginRequestDTO;
import io.centralweb.backend.enums.UserRole;
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
}
