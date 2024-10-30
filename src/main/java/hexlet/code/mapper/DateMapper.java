package hexlet.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class DateMapper {
    public Date toDate(LocalDate createdAt) {
        return java.util.Date.from(createdAt.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
