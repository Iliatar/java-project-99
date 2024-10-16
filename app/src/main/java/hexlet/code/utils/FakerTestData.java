package hexlet.code.utils;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;

public class FakerTestData {

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
                .supply(Select.field(TaskStatus::getName), () -> faker.southPark().characters())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug())
                .create();
        return taskStatus;
    }

    public static String getFakerUserFirstName() {
        return faker.name().firstName();
    }
    public static String getFakerUserLastName() {
        return faker.name().lastName();
    }
}
