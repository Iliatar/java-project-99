package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.mapper.DateMapper;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.FakerTestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {
    //@Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FakerTestData fakerTestData;

    @Autowired
    private DateMapper dateMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    UserMapper userMapper;

    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/users")
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        List<UserDTO> userDTOList = om.readValue(body, new TypeReference<>() { });
        var actual = userDTOList.stream().map(userMapper::map).toList();
        var expected = userRepository.findAll();

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        var request = get("/api/users");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShow() throws Exception {
        var user = fakerTestData.getFakerUser();
        userRepository.save(user);

        var firstName = user.getFirstName();
        var lastName = user.getLastName();
        var email = user.getEmail();
        var createdAt = user.getCreatedAt();

        var request = get("/api/users/" + user.getId())
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
            a -> a.node("firstName").isEqualTo(firstName),
            a -> a.node("lastName").isEqualTo(lastName),
            a -> a.node("email").isEqualTo(email),
            a -> a.node("createdAt").isEqualTo(dateMapper.toString(createdAt))
        );
    }

    @Test
    public void testCreate() throws Exception {
        var testUser = fakerTestData.getFakerUser();
        var createDTO = new UserCreateDTO();
        createDTO.setEmail(testUser.getEmail());
        createDTO.setFirstName(testUser.getFirstName());
        createDTO.setLastName(testUser.getLastName());
        createDTO.setPassword(testUser.getPassword());

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var userId = om.readValue(body, UserDTO.class).getId();

        var savedUser = userRepository.findById(userId).orElse(null);
        assertThat(savedUser.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    public void testUpdate() throws Exception {
        var testUser = fakerTestData.getFakerUser();
        var testUserName = testUser.getUsername();
        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUserName));

        var newFirstName = fakerTestData.getFakerUserFirstName();
        var newLastName = fakerTestData.getFakerUserLastName();
        var newPassword = testUser.getPassword();

        var userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName(JsonNullable.of(newFirstName));
        userUpdateDTO.setLastName(JsonNullable.of(newLastName));
        userUpdateDTO.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/" + testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userUpdateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        testUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(testUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(testUser.getLastName()).isEqualTo(newLastName);
    }

    @Test
    public void testUpdateWithIncorrectData() throws Exception {
        var testUser = fakerTestData.getFakerUser();
        userRepository.save(testUser);

        var newPassword = testUser.getPassword().substring(0, 2);

        var userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/" + testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userUpdateDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteWithIncorrectOwner() throws Exception {
        var testUser = fakerTestData.getFakerUser();
        userRepository.save(testUser);

        var request = delete("/api/users/" + testUser.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDelete() throws Exception {
        var testUser = fakerTestData.getFakerUser();
        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUser.getUsername()));

        var request = delete("/api/users/" + testUser.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var maybeUser = userRepository.findById(testUser.getId());
        assertThat(maybeUser).isNotPresent();
    }

    @Test
    public void testCreateWithEmptyFields() throws Exception {
        var testUser = fakerTestData.getFakerUser();
        var createDTO = new UserCreateDTO();
        createDTO.setEmail(testUser.getEmail());
        createDTO.setFirstName(testUser.getFirstName());
        createDTO.setLastName(testUser.getLastName());

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testShowWithIncorrectId() throws Exception {
        var request = get("/api/users/67986846")
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }
}
