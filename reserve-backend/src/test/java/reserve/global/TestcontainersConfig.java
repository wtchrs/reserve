package reserve.global;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0.36");

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.2.4-alpine");

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(MYSQL_IMAGE).withDatabaseName("db")
            .withUsername("user")
            .withPassword("password")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
    }

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(REDIS_IMAGE).withExposedPorts(6379);
    }

}
