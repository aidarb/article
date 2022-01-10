package com.kae.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kae.article.config.JwtProvider;
import com.kae.article.model.Article;
import com.kae.article.model.Role;
import com.kae.article.model.User;
import com.kae.article.repository.ArticleRepository;
import com.kae.article.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArticlesControllerTest {
    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private final User adminUser;

    private final User usualUser;

    private final List<Article> articles;

    public ArticlesControllerTest() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("1");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        roles.add(Role.ROLE_ADMIN);
        adminUser.setRoles(roles);

        usualUser = new User();
        usualUser.setId(2L);
        usualUser.setUsername("user");
        usualUser.setPassword("1");
        roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        usualUser.setRoles(roles);

        articles = new ArrayList<>();

        for (long i = 0; i < 10; i++) {
            Article article = new Article();
            article.setId(i);
            article.setTitle("Test article title " + i);
            article.setContent("test article content " + i);
            article.setDate(LocalDate.now().plusDays(-i));
            article.setAuthor(adminUser.getUsername());
            article.setUser(adminUser);
            articles.add(article);
        }
    }

    @Test
    public void articleIsOkAndReturnListOfArticle() throws Exception {
        int page = 1;
        int size = 3;
        List<Article> testArticles = articles.subList(page, size);
        Mockito.when(articleRepository.findAll(PageRequest.of(page, size)))
            .thenReturn(new PageImpl<>(testArticles));

        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(adminUser);

        String token = jwtProvider.generateToken(adminUser.getUsername());
        mockMvc.perform(
            MockMvcRequestBuilders.get("/article?page=" +page + "&size=" + size)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(testArticles)));
    }

    @Test
    public void articleByIdIsOkAndReturnArticle() throws Exception {
        Mockito.when(articleRepository.findById(1L))
            .thenReturn(Optional.of(articles.get(0)));

        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(adminUser);

        String token = jwtProvider.generateToken(adminUser.getUsername());
        mockMvc.perform(
            MockMvcRequestBuilders.get("/article/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(articles.get(0))));
    }

    @Test
    public void addArticleIsOkAndReturnArticle() throws Exception {
        Mockito.when(articleRepository.save(articles.get(0)))
            .thenReturn(articles.get(0));

        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(adminUser);

        String token = jwtProvider.generateToken(adminUser.getUsername());
        mockMvc.perform(
            MockMvcRequestBuilders.post("/article")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsBytes(articles.get(0))))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(articles.get(0))));
    }

    @Test
    public void addEmptyArticleIsBadRequestAndReturnValidation() throws Exception {
        Article testArticle = new Article();
        testArticle.setTitle("");
        testArticle.setContent("");
        testArticle.setDate(LocalDate.now());
        testArticle.setAuthor(adminUser.getUsername());
        testArticle.setUser(adminUser);

        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(adminUser);

        String token = jwtProvider.generateToken(adminUser.getUsername());
        mockMvc.perform(
            MockMvcRequestBuilders.post("/article")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsBytes(testArticle)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("Title is mandatory")))
            .andExpect(content().string(Matchers.containsString("Content is mandatory")));
    }

    @Test
    public void adminStatisticsByAdminIsOkAndReturnListOfArticle() throws Exception {
        int page = 1;
        int size = 3;
        LocalDate dateStart = LocalDate.now().minusDays(7);
        LocalDate dateEnd = LocalDate.now();
        List<Article> testArticles = articles
                .stream().filter( article -> article.getDate().compareTo(dateStart) >= 0)
                .collect(Collectors.toList())
                .subList(page, size);

        Mockito.when(articleRepository.findByDateBetween(dateStart, dateEnd, PageRequest.of(page, size)))
            .thenReturn(new PageImpl<>(testArticles));

        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(adminUser);

        String token = jwtProvider.generateToken(adminUser.getUsername());
        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/statistics?page=" +page + "&size=" + size)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(testArticles)));
    }
    @Test
    public void adminStatisticsByUsualUserIsForbidden() throws Exception {
        int page = 1;
        int size = 3;

        Mockito.when(userRepository.findByUsername(usualUser.getUsername()))
            .thenReturn(usualUser);

        String token = jwtProvider.generateToken(usualUser.getUsername());
        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/statistics?page=" +page + "&size=" + size)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }
}