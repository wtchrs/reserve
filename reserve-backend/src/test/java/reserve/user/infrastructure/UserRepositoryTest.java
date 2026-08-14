package reserve.user.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.TestcontainersConfig;
import reserve.user.domain.User;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Testing user deletion by ID")
    void testUserDeletion() {
        User user = userRepository.save(new User("username", "password", "hello", "description"));
        userRepository.deleteById(user.getId());
        assertFalse(userRepository.existsById(user.getId()));
    }

}
