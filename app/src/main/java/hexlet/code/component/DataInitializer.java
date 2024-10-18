package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private static final int TEST_USERS_COUNT_FOR_INDEX = 10;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Faker faker = new Faker();
        for (int i = 0; i < TEST_USERS_COUNT_FOR_INDEX; i++) {
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail(faker.internet().emailAddress());
            user.setPasswordDigest(faker.internet().password());
            userDetailsService.createUser(user);
        }

        var adminUser = new User();
        adminUser.setFirstName("admin");
        adminUser.setLastName("admin");
        adminUser.setEmail("hexlet@example.com");
        adminUser.setPasswordDigest("qwerty");

        userDetailsService.createUser(adminUser);

        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Черновик");
        taskStatus.setSlug("draft");
        taskStatusRepository.save(taskStatus);

        taskStatus = new TaskStatus();
        taskStatus.setName("На проверке");
        taskStatus.setSlug("to_review");
        taskStatusRepository.save(taskStatus);

        taskStatus = new TaskStatus();
        taskStatus.setName("На исправлении");
        taskStatus.setSlug("to_be_fixed");
        taskStatusRepository.save(taskStatus);

        taskStatus = new TaskStatus();
        taskStatus.setName("Готово к выпуску");
        taskStatus.setSlug("to_publish");
        taskStatusRepository.save(taskStatus);

        taskStatus = new TaskStatus();
        taskStatus.setName("Выпущено");
        taskStatus.setSlug("published");
        taskStatusRepository.save(taskStatus);
    }
}
