package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskParamsDTO {
    String titleCont;
    Long assigneeId;
    String status;
    Long labelId;
}
