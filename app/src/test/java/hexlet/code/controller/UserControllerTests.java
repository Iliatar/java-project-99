package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.FakerTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    UserRepository userRepository;

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    public void testShow() throws Exception {
        var user = FakerTestData.getFakerUser();
        userRepository.save(user);

        var firstName = user.getFirstName();
        var lastName = user.getLastName();
        var email = user.getEmail();

        var result = mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
            a -> a.node("firstName").isEqualTo(firstName),
            a -> a.node("lastName").isEqualTo(lastName),
            a -> a.node("email").isEqualTo(email)
        );
    }

    public void testCreate() throws Exception {
        var testUser = FakerTestData.getFakerUser();

        //перемапить User на UserCreateDTO

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var userId = om.readValue(body, UserDTO.class).getId();

        var savedUser = userRepository.findById(userId).get();
        assertThat(savedUser.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    public void testUpdate() throws Exception {
        var testUser = FakerTestData.getFakerUser();
        userRepository.save(testUser);

        var newFirstName = FakerTestData.getFakerUserFirstName();
        var newLastName = FakerTestData.getFakerUserLastName();

        testUser.setFirstName(newFirstName);
        testUser.setLastName(newLastName);

        //перемапить User на UserUpdateDTO

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        testUser = userRepository.findById(testUser.getId()).get();

        assertThat(testUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(testUser.getLastName()).isEqualTo(newLastName);
    }

    public void testDelete() throws Exception {
        var testUser = FakerTestData.getFakerUser();
        userRepository.save(testUser);

        var request = delete("/api/users/" + testUser.getId());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var maybeUser = userRepository.findById(testUser.getId());
        assertThat(maybeUser).isNotPresent();
    }
}
