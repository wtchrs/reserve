package reserve.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Import({ DatabaseCleaner.class, RedisCleaner.class })
public class TestStateCleaner {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private RedisCleaner redisCleaner;

    public void cleanUp() {
        long start = System.nanoTime();

        databaseCleaner.cleanUp();
        long databaseEnd = System.nanoTime();

        redisCleaner.cleanUp();
        long redisEnd = System.nanoTime();

        log.info("Test cleanup: database={} ms, redis={} ms, total={} ms", (databaseEnd - start) / 1_000_000.,
                (redisEnd - databaseEnd) / 1_000_000., (redisEnd - start) / 1_000_000.);
    }

}
