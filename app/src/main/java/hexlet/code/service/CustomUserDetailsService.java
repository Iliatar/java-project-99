package hexlet.code.service;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class CustomUserDetailsService {

    @Autowired
    private UserRepository userRepository;
    public void createUser(User user) {
        var password = user.getPassword();

        try {
            var md = MessageDigest.getInstance("SHA-512");
            password = md.digest(password.getBytes()).toString();
            System.out.println("digest password = " + password);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        user.setPassword(password);
        userRepository.save(user);
    }
}
