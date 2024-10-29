package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    @NotBlank
    JsonNullable<String> title;

    JsonNullable<String> content;

    @NotNull
    JsonNullable<String> status;

    JsonNullable<Long> index;

    @JsonProperty("assignee_id")
    JsonNullable<Long> assigneeId;

    JsonNullable<Set<Long>> taskLabelIds;
}
