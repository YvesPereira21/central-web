package io.centralweb.backend.integrationtest;

import io.centralweb.backend.dto.user.*;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.security.CookieUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
public class AuthenticationTest {
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

    @Test
    public void shouldGenerateTokenCookiesOnLogin(){
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
                .statusCode(HttpStatus.OK.value())
                .cookie(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, notNullValue())
                .cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, notNullValue());
    }

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
    public void shouldRefreshTokenWhenCookieIsValid(){
        LoginRequestDTO login = new LoginRequestDTO(
                "testcentraldev@gmail.com",
                "password"
        );

        // 1. Login to get refresh token cookie
        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login");

        String refreshTokenCookie = loginResponse.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME);

        // 2. Use refresh token cookie to get new access and refresh token cookies
        given()
                .cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshTokenCookie)
        .when()
                .post("/auth/refresh")
        .then()
                .statusCode(HttpStatus.OK.value())
                .cookie(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, notNullValue())
                .cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, notNullValue());
    }

    @Test
    public void shouldNotRefreshTokenWhenCookieDoesNotExist(){
        given()
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

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(login)
        .when()
                .post("/auth/login");

        String refreshTokenCookie = loginResponse.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME);

        // Perform logout with refresh token cookie
        given()
                .cookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshTokenCookie)
        .when()
                .post("/auth/logout")
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
