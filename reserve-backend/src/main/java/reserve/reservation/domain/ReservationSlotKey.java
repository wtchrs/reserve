package reserve.reservation.domain;

import java.time.LocalDate;

public record ReservationSlotKey(Long storeId, LocalDate slotDate, int slotHour) {
    public ReservationSlotKey {
        if (slotHour < 0 || slotHour > 23) {
            throw new IllegalArgumentException("`ReservationSlotKey` must be between from 0 to 23");
        }
    }
}
