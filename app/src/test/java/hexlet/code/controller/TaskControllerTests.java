package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.FakerTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import org.openapitools.jackson.nullable.JsonNullable;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTests {
    private static final String DEFAULT_TASK_STATUS_SLUG = "draft";
    private static final String DEFAULT_LABEL_NAME = "feature";
    private static final String SECONDARY_LABEL_NAME = "bug";
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;


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
        var request = get("/api/tasks")
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray()
                .hasSize(taskRepository.findAll().size());
    }

    @Test
    public void testFilterIndex() throws Exception {
        Task task = FakerTestData.getFakerTask();

        var taskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(taskStatus);

        User user = FakerTestData.getFakerUser();
        userRepository.save(user);
        task.setAssignee(user);

        var label = labelRepository.findByName(DEFAULT_LABEL_NAME).get();
        task.setLabels(Set.of(label));

        taskRepository.save(task);

        var request = get("/api/tasks?titleCont=" + task.getName())
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        System.out.println(body);
        assertThatJson(body)
                .isArray()
                .hasSize(1);
    }

    @Test
    public void testShow() throws Exception {
        Task task = FakerTestData.getFakerTask();
        var taskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(taskStatus);
        var label = labelRepository.findByName(DEFAULT_LABEL_NAME).get();
        task.setLabels(Set.of(label));
        taskRepository.save(task);

        var request = get("/api/tasks/" + task.getId())
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                a -> a.node("title").isEqualTo(task.getName()),
                a -> a.node("content").isEqualTo(task.getDescription()),
                a -> a.node("index").isEqualTo(task.getIndex()),
                a -> a.node("status").isEqualTo(task.getTaskStatus().getSlug()),
                a -> a.node("assignee_id").isAbsent(),
                a -> a.node("taskLabelIds").isArray()
        );
    }

    @Test
    public void testCreate() throws Exception {
        Task task = FakerTestData.getFakerTask();

        var taskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(taskStatus);

        var label = labelRepository.findByName(DEFAULT_LABEL_NAME).get();
        task.setLabels(Set.of(label));

        User user = FakerTestData.getFakerUser();
        userRepository.save(user);
        task.setAssignee(user);

        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setIndex(task.getIndex());
        createDTO.setContent(task.getDescription());
        createDTO.setTitle(task.getName());
        createDTO.setAssigneeId(task.getAssignee().getId());
        createDTO.setStatus(task.getTaskStatus().getSlug());
        createDTO.setTaskLabelIds(Set.of(label.getId()));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var taskId = om.readValue(body, TaskDTO.class).getId();

        Task savedTask = taskRepository.findById(taskId).get();

        assertThat(savedTask.getIndex()).isEqualTo(task.getIndex());
        assertThat(savedTask.getName()).isEqualTo(task.getName());
        assertThat(savedTask.getDescription()).isEqualTo(task.getDescription());
        assertThat(savedTask.getTaskStatus()).isEqualTo(task.getTaskStatus());
        assertThat(savedTask.getAssignee()).isEqualTo(task.getAssignee());
        assertThat(savedTask.getLabels()).isEqualTo(task.getLabels());
    }

    @Test
    public void testUnauthorizedCreate() throws Exception {
        Task task = FakerTestData.getFakerTask();
        var taskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(taskStatus);

        User user = FakerTestData.getFakerUser();
        userRepository.save(user);
        task.setAssignee(user);

        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setIndex(task.getIndex());
        createDTO.setContent(task.getDescription());
        createDTO.setTitle(task.getName());
        createDTO.setAssigneeId(task.getAssignee().getId());
        createDTO.setStatus(task.getTaskStatus().getSlug());

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {

        Task task = FakerTestData.getFakerTask();

        var taskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(taskStatus);

        User user = FakerTestData.getFakerUser();
        userRepository.save(user);
        task.setAssignee(user);

        var label = labelRepository.findByName(DEFAULT_LABEL_NAME).get();
        task.setLabels(Set.of(label));

        taskRepository.save(task);

        user = FakerTestData.getFakerUser();
        userRepository.save(user);

        var secondLabel = labelRepository.findByName(SECONDARY_LABEL_NAME).get();

        Task newTask = FakerTestData.getFakerTask();
        var newTaskStatus = taskStatusRepository.findBySlug("to_be_fixed").get();
        task.setTaskStatus(newTaskStatus);
        task.setTaskStatus(newTaskStatus);
        task.setName(newTask.getName());
        task.setDescription(newTask.getDescription());
        task.setIndex(newTask.getIndex());
        task.setAssignee(user);
        task.setLabels(Set.of(label, secondLabel));

        var updateDTO = new TaskUpdateDTO();
        updateDTO.setAssigneeId(JsonNullable.of(task.getAssignee().getId()));
        updateDTO.setStatus(JsonNullable.of(task.getTaskStatus().getSlug()));
        updateDTO.setTitle(JsonNullable.of(task.getName()));
        updateDTO.setContent(JsonNullable.of(task.getDescription()));
        updateDTO.setIndex(JsonNullable.of(task.getIndex()));
        updateDTO.setTaskLabelIds(JsonNullable.of(Set.of(label.getId(), secondLabel.getId())));

        var request = put("/api/tasks/" + task.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var savedTask = taskRepository.findById(task.getId()).get();
        assertThat(savedTask.getTaskStatus()).isEqualTo(task.getTaskStatus());
        assertThat(savedTask.getIndex()).isEqualTo(task.getIndex());
        assertThat(savedTask.getName()).isEqualTo(task.getName());
        assertThat(savedTask.getDescription()).isEqualTo(task.getDescription());
        assertThat(savedTask.getAssignee()).isEqualTo(task.getAssignee());
        assertThat(savedTask.getLabels()).isEqualTo(task.getLabels());
    }

    @Test
    public void testUpdateWithIncorrectData() throws Exception {
        Task task = FakerTestData.getFakerTask();
        var newTaskStatus = taskStatusRepository.findBySlug("to_be_fixed").get();
        task.setTaskStatus(newTaskStatus);
        taskRepository.save(task);

        var updateDTO = new TaskUpdateDTO();
        updateDTO.setStatus(JsonNullable.of(null));

        var request = put("/api/tasks/" + task.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        Task task = FakerTestData.getFakerTask();
        var newTaskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(newTaskStatus);
        taskRepository.save(task);

        var request = delete("/api/tasks/" + task.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var maybeTask = taskRepository.findById(task.getId());
        assertThat(maybeTask).isNotPresent();
    }

    @Test
    public void testUserDelete() throws Exception {
        User user = FakerTestData.getFakerUser();
        userRepository.save(user);

        token = jwt().jwt(builder -> builder.subject(user.getUsername()));

        Task task = FakerTestData.getFakerTask();
        var newTaskStatus = taskStatusRepository.findBySlug(DEFAULT_TASK_STATUS_SLUG).get();
        task.setTaskStatus(newTaskStatus);
        task.setAssignee(user);
        taskRepository.save(task);

        var request = delete("/api/users/" + user.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTaskStatusDelete() throws Exception {
        var statusSlug = "to_review";
        var status = taskStatusRepository.findBySlug(statusSlug).get();

        Task task = FakerTestData.getFakerTask();
        task.setTaskStatus(status);
        taskRepository.save(task);

        var request = delete("/api/task_statuses/" + status.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }
}
