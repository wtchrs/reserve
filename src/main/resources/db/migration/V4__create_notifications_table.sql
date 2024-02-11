create table notifications
(
    notification_id bigint auto_increment,
    user_id         bigint                  not null,
    resource_type   enum ('RESERVATION')    not null,
    resource_id     bigint                  not null,
    message         varchar(255)            not null,
    status          enum ('UNREAD', 'READ') not null,
    created_at      datetime(6)             not null,
    modified_at     datetime(6)             not null,
    primary key (notification_id),
    constraint fk_notifications_users_userid foreign key (user_id) references users (user_id)
);
