CREATE TABLE notifications
(
    notification_id BIGINT AUTO_INCREMENT,
    user_id         BIGINT                  NOT NULL,
    resource_type   ENUM ('RESERVATION')    NOT NULL,
    resource_id     BIGINT                  NOT NULL,
    message         VARCHAR(255)            NOT NULL,
    status          ENUM ('UNREAD', 'READ') NOT NULL,
    created_at      DATETIME(6)             NOT NULL,
    modified_at     DATETIME(6)             NOT NULL,
    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_users_userid FOREIGN KEY (user_id) REFERENCES users (user_id)
);
