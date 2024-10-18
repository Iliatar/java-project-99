package hexlet.code.utils;

import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FakerTestData {
    @Autowired
    private static TaskStatusRepository taskStatusRepository;

    private static Faker faker = new Faker();
    public static User getFakerUser() {
        User user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password())
                .create();
        return user;
    }

    public static TaskStatus getFakerTaskStatus() {
        TaskStatus taskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.name().lastName())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug())
                .create();
        return taskStatus;
    }

    public static Task getFakerTask(String taskStatusSlug) {
        TaskStatus taskStatus = taskStatusRepository.findBySlug(taskStatusSlug).get();

        Task task = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getIndex), () -> faker.number().numberBetween(1,50))
                .supply(Select.field(Task::getName), () -> faker.lorem().words(3))
                .supply(Select.field(Task::getDescription), () -> faker.lorem().paragraph(10))
                .create();
        return task;
    }

    public static String getFakerUserFirstName() {
        return faker.name().firstName();
    }
    public static String getFakerUserLastName() {
        return faker.name().lastName();
    }
}
