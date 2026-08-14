package reserve;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reserve.global.TestcontainersConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class ReserveBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
