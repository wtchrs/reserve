package reserve.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import({ DatabaseCleaner.class, RedisCleaner.class })
public class TestStateCleaner {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private RedisCleaner redisCleaner;

    public void cleanUp() {
        databaseCleaner.cleanUp();
        redisCleaner.cleanUp();
    }

}
