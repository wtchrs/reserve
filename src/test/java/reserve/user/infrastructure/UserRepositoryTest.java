package reserve.user.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.user.domain.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void deleteById() {
        User user = userRepository.save(new User("username", "password", "hello", "description"));
        userRepository.deleteById(user.getId());
        assertFalse(userRepository.existsById(user.getId()));
    }

}
