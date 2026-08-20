package reserve.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanUp() {
        jdbcTemplate.execute((Connection conn) -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

                try {
                    stmt.execute("TRUNCATE TABLE reservation_menus");
                    stmt.execute("TRUNCATE TABLE notifications");
                    stmt.execute("TRUNCATE TABLE menus");
                    stmt.execute("TRUNCATE TABLE reservations");
                    stmt.execute("TRUNCATE TABLE stores");
                    stmt.execute("TRUNCATE TABLE users");
                }
                finally {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }

            return null;
        });
    }

}
