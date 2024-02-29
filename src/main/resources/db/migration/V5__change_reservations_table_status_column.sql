ALTER TABLE reservations
ADD status_new ENUM ('READY', 'IN_SERVICE', 'COMPLETED', 'CANCELLED') NOT NULL AFTER status;

UPDATE reservations
SET status_new = IF(status = 'AVAILABLE', 'READY', 'CANCELLED');

ALTER TABLE reservations
DROP COLUMN status;

ALTER TABLE reservations
CHANGE COLUMN status_new status ENUM ('READY', 'IN_SERVICE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'READY';
