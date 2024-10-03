package hexlet.code.repository;

import org.instancio.Select;
import org.junit.jupiter.api.Order;
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

    private static User testUser;

    @Test
    @Order(1)
    public void repositoryTest() {
        long recordsCount = repository.count();
        testUser = getFakerUser();
        String firstName = testUser.getFirstName();
        String lastName = testUser.getLastName();
        String email = testUser.getEmail();
        String password = testUser.getPassword();

        repository.save(testUser);
        assertThat(repository.count()).isEqualTo(++recordsCount);

        Long id = testUser.getId();
        testUser = repository.findById(id).get();

        assertThat(testUser.getFirstName()).isEqualTo(firstName);
        assertThat(testUser.getLastName()).isEqualTo(lastName);
        assertThat(testUser.getPassword()).isEqualTo(password);
        assertThat(testUser.getEmail()).isEqualTo(email);
        assertThat(testUser.getCreatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());

        String newName = faker.name().firstName();
        String newLastName = faker.name().lastName();
        testUser.setFirstName(newName);
        testUser.setLastName(newLastName);
        repository.save(testUser);

        testUser = repository.findById(testUser.getId()).get();
        assertThat(testUser.getFirstName()).isEqualTo(newName);
        assertThat(testUser.getLastName()).isEqualTo(newLastName);

        repository.deleteById(testUser.getId());
        assertThat(repository.count()).isEqualTo(--recordsCount);
        assertThat(repository.findById(testUser.getId()).isPresent()).isFalse();
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
