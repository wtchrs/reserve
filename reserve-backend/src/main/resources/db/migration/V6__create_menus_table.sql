CREATE TABLE menus
(
    menu_id     BIGINT AUTO_INCREMENT,
    store_id    BIGINT                        NOT NULL,
    name        VARCHAR(255)                  NOT NULL,
    price       INT                           NOT NULL,
    description TEXT,
    status      ENUM ('AVAILABLE', 'DELETED') NOT NULL,
    created_at  DATETIME(6)                   NOT NULL,
    modified_at DATETIME(6)                   NOT NULL,
    PRIMARY KEY (menu_id),
    CONSTRAINT fk_menus_stores_storeid FOREIGN KEY (store_id) REFERENCES stores (store_id)
);

CREATE TABLE reservation_menus
(
    reservation_menu_id BIGINT AUTO_INCREMENT,
    reservation_id      BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    price               INT          NOT NULL,
    quantity            TINYINT      NOT NULL,
    created_at          DATETIME(6)  NOT NULL,
    modified_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (reservation_menu_id),
    CONSTRAINT fk_reservationmenus_reservations_reservationid
        FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id)
);

ALTER TABLE stores
DROP COLUMN price;
