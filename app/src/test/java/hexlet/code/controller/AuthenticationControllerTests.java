package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
//import hexlet.code.dto.AuthRequest;
import hexlet.code.service.CustomUserDetailsService;
//import hexlet.code.utils.FakerTestData;
//import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    /*@Test
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

        var actualToken = result.getResponse().getContentAsString();
        var expectedToken = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        assertThat(actualToken).isEqualTo(expectedToken);
    }*/
}
