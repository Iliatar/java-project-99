package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.utils.FakerTestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelTests {
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private FakerTestData fakerTestData;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext wac;

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
        var request = get("/api/labels")
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        List<LabelDTO> labelDTOList = om.readValue(body, new TypeReference<>() { });
        var actual = labelDTOList.stream().map(labelMapper::map).toList();
        var expected = labelRepository.findAll();

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        Label label = fakerTestData.getFakerLabel();
        labelRepository.save(label);

        var request = get("/api/labels/" + label.getId())
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                a -> a.node("name").isEqualTo(label.getName())
        );
    }

    @Test
    public void testCreate() throws Exception {
        Label label = fakerTestData.getFakerLabel();

        LabelCreateDTO createDTO = new LabelCreateDTO();
        createDTO.setName(label.getName());

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var labelId = om.readValue(body, LabelDTO.class).getId();

        Label savedLabel = labelRepository.findById(labelId).get();

        assertThat(savedLabel.getName()).isEqualTo(label.getName());
    }

    @Test
    public void testUnauthorizedCreate() throws Exception {
        Label label = fakerTestData.getFakerLabel();

        LabelCreateDTO createDTO = new LabelCreateDTO();
        createDTO.setName(label.getName());

        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        Label label = fakerTestData.getFakerLabel();
        labelRepository.save(label);

        Label newLabel = fakerTestData.getFakerLabel();
        label.setName(newLabel.getName());

        var updateDTO = new LabelUpdateDTO();
        updateDTO.setName(JsonNullable.of(label.getName()));

        var request = put("/api/labels/" + label.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var savedLabel = labelRepository.findById(label.getId()).get();
        assertThat(savedLabel.getName()).isEqualTo(label.getName());
    }

    @Test
    public void testUpdateWithIncorrectData() throws Exception {
        Label label = fakerTestData.getFakerLabel();
        labelRepository.save(label);

        var updateDTO = new LabelUpdateDTO();
        updateDTO.setName(JsonNullable.of("pa"));

        var request = put("/api/labels/" + label.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        Label label = fakerTestData.getFakerLabel();
        labelRepository.save(label);

        var request = delete("/api/labels/" + label.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var maybeLabel = labelRepository.findById(label.getId());
        assertThat(maybeLabel).isNotPresent();
    }
}
