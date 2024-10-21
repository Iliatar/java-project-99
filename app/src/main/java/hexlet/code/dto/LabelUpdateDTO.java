package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class LabelUpdateDTO {
    @Length(min = 3, max = 1000)
    private String name;
}
