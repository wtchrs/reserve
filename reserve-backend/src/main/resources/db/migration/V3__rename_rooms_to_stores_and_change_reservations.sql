ALTER TABLE rooms RENAME stores;

ALTER TABLE stores RENAME INDEX fk_rooms_users_user_id TO fk_stores_users_userid;
ALTER TABLE stores RENAME INDEX fx_rooms_name_address_description TO fx_stores_name_address_description;
ALTER TABLE stores RENAME COLUMN room_id TO store_id;

ALTER TABLE reservations RENAME COLUMN room_id TO store_id;
ALTER TABLE reservations RENAME INDEX fk_reservations_rooms_room_id TO fk_reservations_stores_storeid;

ALTER TABLE reservations DROP COLUMN end_date;
ALTER TABLE reservations RENAME COLUMN start_date TO date;
ALTER TABLE reservations ADD COLUMN hour TINYINT NOT NULL;

ALTER TABLE reservations RENAME INDEX fk_reservations_users_user_id TO fk_reservations_users_userid;
