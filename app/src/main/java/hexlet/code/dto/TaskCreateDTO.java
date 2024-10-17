package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    private Long index;

    private Long assignee_id;

    @NotBlank
    private String title;

    private String content;

    @NotNull
    private String status;
}