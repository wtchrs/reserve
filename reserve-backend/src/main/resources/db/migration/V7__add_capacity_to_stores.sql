/*
 * Add hourly reservation capacity to stores.
 */

ALTER TABLE stores
ADD COLUMN capacity INT NOT NULL DEFAULT -1
CHECK (capacity > -2);
