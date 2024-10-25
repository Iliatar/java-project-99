package hexlet.code.controller;

import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskSpecification specBuilder;

    @GetMapping(path = "")
    public ResponseEntity<List<TaskDTO>> index(TaskParamsDTO paramsDTO) {
        var spec = specBuilder.build(paramsDTO);
        var statusList = taskRepository.findAll(spec)
                .stream()
                .map(taskMapper::map)
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statusList.size()))
                .body(statusList);
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO show(@PathVariable Long id) {
        var taskStatus = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        return taskMapper.map(taskStatus);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@RequestBody @Valid TaskCreateDTO createDTO) {
        var model = taskMapper.map(createDTO);
        taskRepository.save(model);
        var dto = taskMapper.map(model);
        return dto;
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO update(@RequestBody @Valid TaskUpdateDTO updateDTO, @PathVariable Long id) {
        var model = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        taskMapper.update(updateDTO, model);
        taskRepository.save(model);
        var dto = taskMapper.map(model);
        return dto;
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskRepository.deleteById(id);
    }
}
