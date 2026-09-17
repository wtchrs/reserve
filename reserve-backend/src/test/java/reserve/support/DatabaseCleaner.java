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
                    stmt.execute("DELETE FROM reservation_menus");
                    stmt.execute("DELETE FROM notifications");
                    stmt.execute("DELETE FROM menus");
                    stmt.execute("DELETE FROM reservations");
                    stmt.execute("DELETE FROM stores");
                    stmt.execute("DELETE FROM users");
                }
                finally {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }

            return null;
        });
    }

}
