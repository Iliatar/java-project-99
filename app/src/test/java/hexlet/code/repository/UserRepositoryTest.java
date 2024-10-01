package hexlet.code.repository;

import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.instancio.Instancio;
import net.datafaker.Faker;

import hexlet.code.model.User;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository repository;

    @Autowired
    private Faker faker;

    private long recordsCount;

    @Test
    public void createTest() {
        recordsCount = repository.count();
        User user = getFakerUser();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        String password = user.getPassword();

        repository.save(user);
        assertThat(repository.count()).isEqualTo(++recordsCount);

        Long id = user.getId();
        user = repository.findById(id).get();

        assertThat(user.getFirstName()).isEqualTo(firstName);
        assertThat(user.getLastName()).isEqualTo(lastName);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getCreatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
    }

    private User getFakerUser() {
        User user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> faker.internet().password())
                .create();
        return user;
    }
}
