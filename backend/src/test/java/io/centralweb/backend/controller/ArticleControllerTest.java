package io.centralweb.backend.controller;

import io.centralweb.backend.dto.article.ArticleCreateDTO;
import io.centralweb.backend.dto.article.ArticleUpdateDTO;
import io.centralweb.backend.enums.UserRole;
import io.centralweb.backend.model.Article;
import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.Tag;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.ArticleRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ArticleControllerTest {
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
    private ArticleRepository articleRepository;
    private String tokenAdmin;
    private String tokenPerson1;
    private String tokenPerson2;
    private UUID article1Id;
    private UUID profile3Id;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        articleRepository.deleteAll();
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
        Profile profileSaved3 = profileRepository.save(profile2);
        profile3Id = profileSaved3.getProfileId();

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

        Article article1 = new Article();
        article1.setTitle("Diferenças entre Python x Java");
        article1.setContent("Content 1");
        article1.setCreatedAt(LocalDate.now());
        article1.setPublished(true);
        article1.setTags(List.of(tagJava, tagPython));
        article1.setProfile(profile1);
        Article articleSaved1 = articleRepository.save(article1);
        article1Id = articleSaved1.getArticleId();

        Article article2 = new Article();
        article2.setTitle("Como implementar index no Spring");
        article2.setContent("Content 2");
        article2.setCreatedAt(LocalDate.now());
        article2.setPublished(true);
        article2.setTags(List.of(tagSpring));
        article2.setProfile(profile2);
        articleRepository.save(article2);

        Article article3 = new Article();
        article3.setTitle("Como implementar redis no Python");
        article3.setContent("Content 2");
        article3.setCreatedAt(LocalDate.now());
        article3.setPublished(true);
        article3.setTags(List.of(tagPython));
        article3.setProfile(profile2);
        articleRepository.save(article3);

        tokenAdmin = tokenService.generateToken(user1);
        tokenPerson1 = tokenService.generateToken(user2);
        tokenPerson2 = tokenService.generateToken(user3);
    }

    // -----------------------HAPPY PATH------------------------------

    @Test
    public void shouldCreateArticleWhenUserIsPerson(){
        ArticleCreateDTO payload = new ArticleCreateDTO(
                "Teste de integração",
                "Conteúdo extenso do artigo para passar na validação...",
                List.of("Java", "Spring")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/articles")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("title", equalTo("Teste de integração"))
                .body("content", equalTo("Conteúdo extenso do artigo para passar na validação..."))
                .body("tags", hasSize(2))
                .body("tags.technologyName", hasItems("Java", "Spring"));
    }

    @Test
    public void shouldReturnArticle(){
        UUID articleId = article1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Diferenças entre Python x Java"))
                .body("content", equalTo("Content 1"))
                .body("tags", hasSize(2))
                .body("tags.technologyName", hasItems("Java", "Python"));
    }

    @Test
    public void shouldReturnArticlesPublished(){
        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/articles")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(3));
    }

    @Test
    public void shouldReturnArticlesPublishedWithGivenTitle(){
        String title = "implementar index";

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .get("/articles/" + title + "/title")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(1));
    }

    @Test
    public void shouldReturnArticlesPublishedWithGivenTechnologyName(){
        String technologyName = "Python";

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .when()
                .get("/articles/" + technologyName + "/tag")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(2));
    }

    @Test
    public void shouldReturnArticlesPublishedByCertainProfile(){
        UUID profileId = profile3Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .when()
                .get("/articles/" + profileId + "/profile")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", is(2));
    }

    @Test
    public void shouldUpdateArticleWhenUserIsOwner(){
        UUID articleId = article1Id;
        ArticleUpdateDTO payload = new ArticleUpdateDTO(
                "Teste de integração 123",
                "Este conteúdo foi atualizado para ver se funciona",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .put("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Teste de integração 123"))
                .body("content", equalTo("Este conteúdo foi atualizado para ver se funciona"))
                .body("tags", hasSize(1))
                .body("tags.technologyName", hasItem("Python"));
    }

    @Test
    public void shouldToggleLikeWhenUserIsPerson(){
        UUID articleId = article1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .patch("/articles/" + articleId + "/like")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldDeleteArticleWhenUserIsOwner(){
        UUID articleId = article1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
        .when()
                .delete("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldDeleteArticleWhenUserIsAdmin(){
        UUID articleId = article1Id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .delete("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    // -----------------------UNHAPPY PATH------------------------------

    @Test
    public void shouldNotCreateArticleWhenUserIsNotAuthenticated(){
        ArticleCreateDTO article = new ArticleCreateDTO(
                "Como fazer partição de lista",
                "Para isso você consegue fazer na linguagem Python dessa forma...",
                List.of("Python")
        );

        given()
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .post("/articles")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotCreateArticleWhenUserIsAdmin(){
        ArticleCreateDTO article = new ArticleCreateDTO(
                "Como fazer partição de lista",
                "Para isso você consegue fazer na linguagem Python dessa forma...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .post("/articles")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotCreateArticleWithMissingData(){
        ArticleCreateDTO article = new ArticleCreateDTO(
                "Como integrar o backend com o front",
                "Para você implementar isso tem de fazer assim...",
                List.of()
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .post("/articles")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateArticleWhenTechnologyNameDoesNotExist(){
        ArticleCreateDTO article = new ArticleCreateDTO(
                "Como fazer partição de lista",
                "Para isso você consegue fazer na linguagem Rust desse jeito...",
                List.of("Rust")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .post("/articles")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotUpdateArticleWhenUserIsNotAuthenticated(){
        UUID articleId = article1Id;
        ArticleUpdateDTO article = new ArticleUpdateDTO(
                "Frameworks Python para web",
                "Existem vários frameworks web para Python que são muito utilizados...",
                List.of("Python")
        );

        given()
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .put("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotUpdateArticleWhenDoesNotExist(){
        UUID articleId = UUID.randomUUID();
        ArticleUpdateDTO article = new ArticleUpdateDTO(
                "Como criar um dicionário no Python",
                "Para você criar um dicionário no python, é preciso fazer isso...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .put("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotUpdateArticleWheUserIsNotOwner(){
        UUID articleId = article1Id;
        ArticleUpdateDTO article = new ArticleUpdateDTO(
                "Como criar um dicionário no Python",
                "Para você criar um dicionário no python, é preciso fazer isso...",
                List.of("Python")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .put("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotUpdateArticleWhenTechnologyNameDoesNotExist(){
        UUID articleId = article1Id;
        ArticleUpdateDTO article = new ArticleUpdateDTO(
                "CPython usando Rust",
                "Uma notícia que bombou recentemente é o Python trocando o C pelo Rust...",
                List.of("Python", "Rust")
        );

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
                .body(article)
        .when()
                .put("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotToggleLikeWhenArticleDoesNotExist(){
        UUID articleId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
        .when()
                .patch("/articles/" + articleId + "/like")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotToggleLikeWhenUserIsAdmin(){
        UUID articleId = article1Id;

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .patch("/articles/" + articleId + "/like")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldNotDeleteArticleWhenUserIsNotAuthenticated(){
        UUID articleId = article1Id;

        given()
                .contentType(ContentType.JSON)
        .when()
                .delete("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotDeleteArticleWhenDoesNotExist(){
        UUID articleId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + tokenPerson1)
                .contentType(ContentType.JSON)
        .when()
                .delete("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldNotDeleteArticleWhenUserIsNotOwnerOrIsNotAdmin(){
        UUID articleId = article1Id;

        given()
                .header("Authorization", "Bearer " + tokenPerson2)
        .when()
                .delete("/articles/" + articleId)
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
