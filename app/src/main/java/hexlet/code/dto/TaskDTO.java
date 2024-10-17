package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Long index;
    private Date createdAt;
    private Long assignee_id;
    private String title;
    private String content;
    private String status;
}