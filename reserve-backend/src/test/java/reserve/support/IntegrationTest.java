package reserve.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainerInitializer.class)
public @interface IntegrationTest {

}
