package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.AuthRequest;
import hexlet.code.service.CustomUserDetailsService;
import hexlet.code.utils.FakerTestData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Test
    public void testLoginFailed() throws Exception {
        var request = get("/api/users")
                .header("Authorization", "Bearer tram pam pam");

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin() throws Exception {
        var testUser = FakerTestData.getFakerUser();
        userDetailsService.createUser(testUser);

        AuthRequest requestDTO = new AuthRequest();
        requestDTO.setUsername(testUser.getEmail());
        requestDTO.setPassword(testUser.getPassword());

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(requestDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var token = result.getResponse().getContentAsString();

        request = get("/api/users")
                .header("Authorization", "Bearer " + token);

        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
//        var expectedToken = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
//        assertThat(actualToken).isEqualTo(expectedToken);
    }
}
