package hexlet.code.mapper;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeforeMapping;
import hexlet.code.model.User;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeMapping
    public void encryptPassword(UserCreateDTO createDTO) {
        var password = passwordEncoder.encode(createDTO.getPassword());
        createDTO.setPassword(password);
    }

    @BeforeMapping
    public void encryptPassword(UserUpdateDTO updateDTO) {
        var jsonNullable = updateDTO.getPassword();
        if (jsonNullable == null || !jsonNullable.isPresent() || jsonNullable.get() == null) {
            return;
        }

        var password = passwordEncoder.encode(jsonNullable.get());
        updateDTO.setPassword(JsonNullable.of(password));
    }
}
