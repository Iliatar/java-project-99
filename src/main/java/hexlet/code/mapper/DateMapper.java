package hexlet.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class DateMapper {
    /*public Date toDate(LocalDate localDate) {
        return java.util.Date.from(createdAt.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }*/

    public String toString(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public LocalDate toLocalDateTime(String string) {
        return LocalDate.parse(string);
    }
}
