package com.kae.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kae.article.config.JwtProvider;
import com.kae.article.controller.UserAuth;
import com.kae.article.model.Role;
import com.kae.article.model.User;
import com.kae.article.repository.UserRepository;
import com.kae.article.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginAndRegistrationTest {
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private final User adminUser;

    public LoginAndRegistrationTest() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("1");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        roles.add(Role.ROLE_ADMIN);
        adminUser.setRoles(roles);
    }

    @Test
    public void registrationDataIsEmptyAndReturnValidation() throws Exception {
        UserAuth userAuth = new UserAuth("", "");
        mockMvc.perform(MockMvcRequestBuilders.post("/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userAuth)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("Username is mandatory")))
            .andExpect(content().string(Matchers.containsString("Password is mandatory")));
    }

    @Test
    public void registrationUserIsExistInDbAndReturnBadRequest() throws Exception {
        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(adminUser);

        UserAuth userAuth = new UserAuth(adminUser.getUsername(), "1234");
        mockMvc.perform(MockMvcRequestBuilders.post("/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userAuth)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("User Exists!")));
    }

    @Test
    public void successLoginAndReturnToken() throws Exception {
        Mockito.when(userService.findByLoginAndPassword(adminUser.getUsername(), adminUser.getPassword()))
            .thenReturn(adminUser);
        UserAuth userAuth = new UserAuth(adminUser.getUsername(), adminUser.getPassword());
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userAuth)))
            .andExpect(status().isOk())
            .andExpect(content().string(jwtProvider.generateToken(adminUser.getUsername())));
    }

    @Test
    public void failedLoginAndReturnUnauthorized() throws Exception {
        Mockito.when(userRepository.findByUsername(adminUser.getUsername()))
            .thenReturn(null);

        UserAuth userAuth = new UserAuth(adminUser.getUsername(), "2");
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userAuth)))
            .andExpect(status().isUnauthorized());
    }
}