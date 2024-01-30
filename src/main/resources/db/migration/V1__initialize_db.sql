CREATE TABLE users
(
    user_id       BIGINT AUTO_INCREMENT,
    username      VARCHAR(25)                  NOT NULL,
    password_hash VARCHAR(255)                 NOT NULL,
    nickname      VARCHAR(30)                  NOT NULL,
    description   TEXT,
    created_at    DATETIME(6)                  NOT NULL,
    modified_at   DATETIME(6)                  NOT NULL,
    status        enum ('available','deleted') not null,
    PRIMARY KEY pk_users_user_id (user_id),
    UNIQUE INDEX ux_users_username (username)
);

CREATE TABLE rooms
(
    room_id     BIGINT AUTO_INCREMENT,
    user_id     BIGINT                       NOT NULL,
    name        VARCHAR(255)                 NOT NULL,
    price       INT                          NOT NULL,
    address     VARCHAR(255)                 NOT NULL,
    description TEXT,
    created_at  DATETIME(6)                  NOT NULL,
    modified_at DATETIME(6)                  NOT NULL,
    status      enum ('available','deleted') not null,
    PRIMARY KEY pk_rooms_room_id (room_id),
    FOREIGN KEY fk_rooms_users_user_id (user_id) REFERENCES users (user_id)
);

CREATE TABLE reservations
(
    reservation_id BIGINT AUTO_INCREMENT,
    user_id        BIGINT                       NOT NULL,
    room_id        BIGINT                       NOT NULL,
    start_date     DATE                         NOT NULL,
    end_date       DATE                         NOT NULL,
    created_at     DATETIME(6)                  NOT NULL,
    modified_at    DATETIME(6)                  NOT NULL,
    status         enum ('available','deleted') not null,
    PRIMARY KEY pk_reservations_reservation_id (reservation_id),
    FOREIGN KEY fk_reservations_users_user_id (user_id) REFERENCES users (user_id),
    FOREIGN KEY fk_reservations_rooms_room_id (room_id) REFERENCES rooms (room_id)
);
