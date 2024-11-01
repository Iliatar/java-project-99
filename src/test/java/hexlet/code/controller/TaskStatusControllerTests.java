package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTests {
    //@Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FakerTestData fakerTestData;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private TaskStatusMapper mapper;

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
        var request = get("/api/task_statuses")
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        List<TaskStatusDTO> statusDTOList = om.readValue(body, new TypeReference<>() { });
        var actual = statusDTOList.stream().map(mapper::map).toList();
        var expected = taskStatusRepository.findAll();

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        TaskStatus taskStatus = fakerTestData.getFakerTaskStatus();
        taskStatusRepository.save(taskStatus);

        var request = get("/api/task_statuses/" + taskStatus.getId())
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                a -> a.node("name").isEqualTo(taskStatus.getName()),
                a -> a.node("slug").isEqualTo(taskStatus.getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var taskStatus = fakerTestData.getFakerTaskStatus();
        var taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setSlug(taskStatus.getSlug());
        taskStatusCreateDTO.setName(taskStatus.getName());

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskStatusCreateDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var statusId = om.readValue(body, TaskStatusDTO.class).getId();

        var savedTask = taskStatusRepository.findById(statusId).orElse(null);
        assertThat(savedTask.getSlug()).isEqualTo(taskStatus.getSlug());
        assertThat(savedTask.getName()).isEqualTo(taskStatus.getName());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var taskStatus = fakerTestData.getFakerTaskStatus();
        var taskStatusCreateDTO = mapper.map(taskStatus);

        var request = post("/api/task_statuses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskStatusCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate()  throws Exception {
        var taskStatus = fakerTestData.getFakerTaskStatus();
        taskStatusRepository.save(taskStatus);

        var newTaskStatus = fakerTestData.getFakerTaskStatus();
        var statusUpdateDTO = new TaskStatusUpdateDTO();
        statusUpdateDTO.setName(JsonNullable.of(newTaskStatus.getName()));
        statusUpdateDTO.setSlug(JsonNullable.of(newTaskStatus.getSlug()));

        var request = put("/api/task_statuses/" + taskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(statusUpdateDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk());

        taskStatus = taskStatusRepository.findById(taskStatus.getId()).orElse(null);
        assertThat(taskStatus.getName()).isEqualTo(newTaskStatus.getName());
    }

    @Test
    public void testUpdateWithIncorrectData() throws Exception {
        var taskStatus = fakerTestData.getFakerTaskStatus();
        taskStatusRepository.save(taskStatus);

        var newTaskStatus = fakerTestData.getFakerTaskStatus();
        var statusUpdateDTO = new TaskStatusUpdateDTO();
        statusUpdateDTO.setName(JsonNullable.of(""));
        statusUpdateDTO.setSlug(JsonNullable.of(newTaskStatus.getSlug()));

        var request = put("/api/task_statuses/" + taskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(statusUpdateDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        var taskStatus = fakerTestData.getFakerTaskStatus();
        taskStatusRepository.save(taskStatus);

        var request = delete("/api/task_statuses/" + taskStatus.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var maybeStatus = taskStatusRepository.findById(taskStatus.getId());
        assertThat(maybeStatus).isNotPresent();
    }
}
