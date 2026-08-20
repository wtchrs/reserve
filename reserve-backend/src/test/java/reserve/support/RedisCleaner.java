package reserve.support;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisCleaner {

    private final RedisConnectionFactory connectionFactory;

    public RedisCleaner(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void cleanUp() {
        try (RedisConnection conn = connectionFactory.getConnection()) {
            conn.serverCommands().flushDb();
        }
    }

}
