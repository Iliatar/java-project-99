package hexlet.code.mapper;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.BeforeMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import hexlet.code.model.User;

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
    public abstract User map(UserCreateDTO createDTO);
    public abstract void update(UserUpdateDTO updateDTO, @MappingTarget User user);

    @BeforeMapping
    public void encryptPassword(UserCreateDTO createDTO) {
        var password = createDTO.getPassword();
        try {
            var md = MessageDigest.getInstance("SHA-512");
            password = md.digest(password.getBytes()).toString();
            System.out.println("digest password = " + password);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        createDTO.setPassword(password);
    }
}
