package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskUpdateDTO {
    @NotBlank
    JsonNullable<String> title;

    JsonNullable<String> content;

    @NotNull
    JsonNullable<String> status;

    JsonNullable<Long> index;

    JsonNullable<Long> assignee_id;
}
