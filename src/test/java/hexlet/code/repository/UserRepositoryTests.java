package hexlet.code.repository;

import hexlet.code.utils.FakerTestData;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import net.datafaker.Faker;

import hexlet.code.model.User;
//import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserRepositoryTests {
    @Autowired
    private UserRepository repository;

    @Autowired
    private Faker faker;

    private static User testUser;

    @Test
    @Order(1)
    public void repositoryTest() {
        long recordsCount = repository.count();
        testUser = FakerTestData.getFakerUser();
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
        //assertThat(testUser.getCreatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());

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
}
