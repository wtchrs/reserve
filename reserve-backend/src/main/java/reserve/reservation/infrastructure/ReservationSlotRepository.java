package reserve.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class ReservationSlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public void createIfAbsent(Long storeId, LocalDate slotDate, int slotHour) {
        String sql = """
                INSERT INTO reservation_slots (store_id, slot_date, slot_hour)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE store_id = store_id;
                """;

        jdbcTemplate.update(sql, storeId, slotDate, slotHour);
    }

    public boolean tryAcquire(Long storeId, LocalDate slotDate, int slotHour) {
        String sql = """
                UPDATE reservation_slots rs
                JOIN stores s ON s.store_id = rs.store_id
                SET rs.slot_count = rs.slot_count + 1
                WHERE rs.store_id = ?
                  AND rs.slot_date = ?
                  AND rs.slot_hour = ?
                  AND (s.capacity = -1 OR rs.slot_count < s.capacity);
                """;

        int affected = jdbcTemplate.update(sql, storeId, slotDate, slotHour);
        return affected == 1;
    }

    public void release(Long storeId, LocalDate slotDate, int slotHour) {
        String sql = """
                UPDATE reservation_slots rs
                JOIN stores s ON s.store_id = rs.store_id
                SET rs.slot_count = rs.slot_count - 1
                WHERE rs.store_id = ?
                  AND rs.slot_date = ?
                  AND rs.slot_hour = ?;
                """;

        jdbcTemplate.update(sql, storeId, slotDate, slotHour);
    }

}
