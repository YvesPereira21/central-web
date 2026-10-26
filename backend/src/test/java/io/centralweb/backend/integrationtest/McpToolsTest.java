package io.centralweb.backend.integrationtest;

import io.centralweb.backend.model.*;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.*;
import io.centralweb.backend.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class McpToolsTest {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired ProfileRepository profiles;
    @Autowired QuestionRepository questions;
    @Autowired ArticleRepository articles;
    @Autowired AnswerRepository answers;
    @Autowired CommentRepository comments;
    @Autowired CommentService commentService;

    @Test
    void anonymousClientCanSearchOnlyPublishedContent() throws Exception {
        User author = new User();
        author.setEmail(UUID.randomUUID() + "@example.com");
        author.setPassword("password");
        author.setRole(UserRole.PERSON);
        Profile profile = new Profile();
        profile.setName("Author");
        profile.setUser(author);
        profiles.save(profile);
        Question question = new Question();
        question.setTitle("Spring example");
        question.setContent("Example content");
        question.setProfile(profile);
        question.setPublished(true);
        questions.saveAndFlush(question);
        Question draftQuestion = new Question();
        draftQuestion.setTitle("Spring draft question");
        draftQuestion.setContent("Private draft question");
        draftQuestion.setProfile(profile);
        draftQuestion.setPublished(false);
        questions.saveAndFlush(draftQuestion);
        Article article = new Article();
        article.setTitle("Spring article");
        article.setContent("Published article content");
        article.setProfile(profile);
        article.setPublished(true);
        articles.saveAndFlush(article);
        Article draftArticle = new Article();
        draftArticle.setTitle("Spring draft article");
        draftArticle.setContent("Private draft article");
        draftArticle.setProfile(profile);
        draftArticle.setPublished(false);
        articles.saveAndFlush(draftArticle);
        Answer answer = createAnswer(question, profile, "Published answer");
        Answer draftAnswer = createAnswer(draftQuestion, profile, "Draft answer");
        createComment(answer, profile, "Published comment");
        createComment(draftAnswer, profile, "Draft comment");
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        String initialize = """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{
                  "protocolVersion":"2025-03-26","capabilities":{},
                  "clientInfo":{"name":"test","version":"1"}}}
                """;
        mvc.perform(post("/questions")).andExpect(status().isForbidden());
        var response = mvc.perform(post("/mcp")
                .contentType("application/json").header("Accept", "application/json, text/event-stream")
                .content(initialize)).andExpect(status().isOk()).andReturn().getResponse();
        String session = response.getHeader("Mcp-Session-Id");
        assertThat(session).isNotBlank();
        mvc.perform(post("/mcp").header("Mcp-Session-Id", session)
                .contentType("application/json").header("Accept", "application/json, text/event-stream")
                .content("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}"))
                .andExpect(status().isAccepted());
        var tools = mvc.perform(post("/mcp").header("Mcp-Session-Id", session)
                .contentType("application/json").header("Accept", "application/json, text/event-stream")
                .content("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(tools).contains("search_questions", "search_articles", "search_answers", "search_comments");
        assertThat(callTool(mvc, session, "search_questions", "\"termo\":\"spring\",\"pagina\":0"))
                .contains("Spring example").doesNotContain("Spring draft question", "Private draft question");
        assertThat(callTool(mvc, session, "search_articles", "\"termo\":\"spring\",\"pagina\":0"))
                .contains("Spring article").doesNotContain("Spring draft article", "Private draft article");
        assertThat(callTool(mvc, session, "search_answers", "\"perguntaId\":\"" + question.getQuestionId() + "\",\"pagina\":0"))
                .contains("Published answer");
        assertThat(callTool(mvc, session, "search_answers", "\"perguntaId\":\"" + draftQuestion.getQuestionId() + "\",\"pagina\":0"))
                .doesNotContain("Draft answer");
        assertThat(callTool(mvc, session, "search_comments", "\"respostaId\":\"" + answer.getAnswerId() + "\",\"pagina\":0"))
                .contains("Published comment");
        assertThat(callTool(mvc, session, "search_comments", "\"respostaId\":\"" + draftAnswer.getAnswerId() + "\",\"pagina\":0"))
                .doesNotContain("Draft comment");
    }

    private String callTool(MockMvc mvc, String session, String name, String arguments) throws Exception {
        String call = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\""
                + name + "\",\"arguments\":{" + arguments + "}}}";
        String result = mvc.perform(post("/mcp").header("Mcp-Session-Id", session)
                .contentType("application/json").header("Accept", "application/json, text/event-stream")
                .content(call)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(result).contains("result", "items").doesNotContain("isError\":true");
        return result;
    }

    private Answer createAnswer(Question question, Profile profile, String content) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setProfile(profile);
        answer.setContent(content);
        return answers.saveAndFlush(answer);
    }

    private void createComment(Answer answer, Profile profile, String content) {
        Comment comment = new Comment();
        comment.setAnswer(answer);
        comment.setProfile(profile);
        comment.setContent(content);
        comments.saveAndFlush(comment);
    }

    @Test
    @Transactional
    void commentsAreReturnedOnlyForPublishedQuestions() {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(UserRole.PERSON);
        users.save(user);
        Profile profile = new Profile();
        profile.setName("Author");
        profile.setUser(user);
        profiles.save(profile);
        Question question = new Question();
        question.setTitle("Question");
        question.setContent("Content");
        question.setProfile(profile);
        question.setPublished(true);
        questions.saveAndFlush(question);
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setProfile(profile);
        answer.setContent("Answer");
        answers.saveAndFlush(answer);
        Comment comment = new Comment();
        comment.setAnswer(answer);
        comment.setProfile(profile);
        comment.setContent("Comment");
        comments.saveAndFlush(comment);
        assertThat(commentService.getCommentsFromPublishedQuestion(answer.getAnswerId(), PageRequest.of(0, 5)).getContent()).hasSize(1);
        question.setPublished(false);
        questions.saveAndFlush(question);
        assertThat(commentService.getCommentsFromPublishedQuestion(answer.getAnswerId(), PageRequest.of(0, 5)).getContent()).isEmpty();
    }
}
