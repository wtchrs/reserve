/*
 * Creates reservation slot table used to restrict reservations up to store capacity.
 */

CREATE TABLE reservation_slots
(
    reservation_slot_id BIGINT AUTO_INCREMENT,
    store_id            BIGINT  NOT NULL,
    slot_date           DATE    NOT NULL,
    slot_hour           TINYINT NOT NULL,
    slot_count          INT     NOT NULL DEFAULT 0,
    PRIMARY KEY (reservation_slot_id),
    CONSTRAINT ux_reservationslots_storeid_date_hour UNIQUE (store_id, slot_date, slot_hour),
    CONSTRAINT fk_reservationslots_stores_store_id FOREIGN KEY (store_id) REFERENCES stores (store_id)
);

INSERT INTO reservation_slots (store_id, slot_date, slot_hour, slot_count)
SELECT store_id, date, hour, COUNT(*)
FROM reservations
WHERE status <> 'CANCELLED'
GROUP BY store_id, date, hour;
