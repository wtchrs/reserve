package reserve.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class TestcontainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.36"))
        .withDatabaseName("db")
        .withUsername("user")
        .withPassword("password")
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    public static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.2.4-alpine"))
        .withExposedPorts(6379);

    static {
        MYSQL.start();
        REDIS.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues
            .of("spring.datasource.url=" + MYSQL.getJdbcUrl(), "spring.datasource.username=" + MYSQL.getUsername(),
                    "spring.datasource.password=" + MYSQL.getPassword(), "spring.data.redis.host=" + REDIS.getHost(),
                    "spring.data.redis.port=" + REDIS.getMappedPort(6379), "spring.data.redis.username=",
                    "spring.data.redis.password=")
            .applyTo(ctx.getEnvironment());
    }

}
