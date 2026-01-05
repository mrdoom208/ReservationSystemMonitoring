package com.mycompany.reservationsystem.service;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean adminExists() {
        return userRepository.existsByPosition(User.Position.ADMINISTRATOR);
    }

    public User createAdminIfMissing() {
        if (!adminExists()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // hash in production!
            admin.setFirstname("Default");
            admin.setLastname("Admin");
            admin.setPosition(User.Position.ADMINISTRATOR);
            admin.setStatus("ACTIVE");

            return userRepository.save(admin);
        }
        return null; // admin already exists
    }

    // Optional: other user methods
    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByUsernameAndPassword(String username,String password) {
        return userRepository.findByUsernameAndPassword(username,password);
    }
}
