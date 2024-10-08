package hexlet.code.mapper;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import org.mapstruct.*;
import hexlet.code.model.User;
import org.openapitools.jackson.nullable.JsonNullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Mapper (
        uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    public abstract UserDTO map(User user);
    @Mapping(target = "passwordDigest", source = "password")
    public abstract User map(UserCreateDTO createDTO);
    public abstract void update(UserUpdateDTO updateDTO, @MappingTarget User user);

    @BeforeMapping
    public void encryptPassword(UserCreateDTO createDTO) {
        var password = createDTO.getPassword();
        try {
            var md = MessageDigest.getInstance("SHA-512");
            password = md.digest(password.getBytes()).toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        createDTO.setPassword(password);
    }

    @BeforeMapping
    public void encryptPassword(UserUpdateDTO updateDTO) {
        var jsonNullable = updateDTO.getPassword();
        if (!jsonNullable.isPresent() || jsonNullable.get() == null) {
            return;
        }

        var password = jsonNullable.get();

        try {
            var md = MessageDigest.getInstance("SHA-512");
            password = md.digest(password.getBytes()).toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        updateDTO.setPassword(JsonNullable.of(password));
    }
}
