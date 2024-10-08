package hexlet.code.component;

import hexlet.code.model.User;
import hexlet.code.service.CustomUserDetailsService;
import hexlet.code.utils.FakerTestData;
import lombok.AllArgsConstructor;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (int i = 0; i < TEST_USERS_COUNT_FOR_INDEX; i++) {
            userDetailsService.createUser(FakerTestData.getFakerUser());
        }

        var adminUser = new User();
        adminUser.setFirstName("admin");
        adminUser.setLastName("admin");
        adminUser.setEmail("hexlet@example.com");
        adminUser.setPasswordDigest("qwerty");

        userDetailsService.createUser(adminUser);
    }
}
