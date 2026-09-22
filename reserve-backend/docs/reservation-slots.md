# Reservation Slots

The reservation slot table and store capacity column were introduced in the V7 migration to enforce
each store's hourly reservation capacity safely when concurrent requests are made.

## Store capacity

`stores.capacity` has the following meaning:

| Value | Meaning |
| --- | --- |
| `-1` | Reservations can be created without a limit. |
| `0` | Reservations cannot be created. |
| `N` (`N > 0`) | Up to `N` reservations can be created for a slot. |

## Creating a slot

Create the slot for the requested store, date, and hour if it does not already exist:

```sql
INSERT INTO reservation_slots (store_id, slot_date, slot_hour)
VALUES (:storeId, :date, :hour)
ON DUPLICATE KEY UPDATE store_id = store_id;
```

## Creating a reservation

Increment the slot count only when the store still has capacity:

```sql
UPDATE reservation_slots rs
JOIN stores s ON s.store_id = rs.store_id
SET rs.slot_count = rs.slot_count + 1
WHERE rs.store_id = :storeId
  AND rs.slot_date = :date
  AND rs.slot_hour = :hour
  AND (s.capacity = -1 OR rs.slot_count < s.capacity);
```

The application must verify that exactly one row was affected. If no row was affected, the slot has
reached its capacity or does not exist, and the reservation must not be created.

## Canceling a reservation

Decrement the count when a reservation is canceled or moved to another slot:

```sql
UPDATE reservation_slots rs
JOIN stores s ON s.store_id = rs.store_id
SET rs.slot_count = rs.slot_count - 1
WHERE rs.store_id = :storeId
  AND rs.slot_date = :date
  AND rs.slot_hour = :hour;
```

This query must run in the same transaction as the reservation cancellation or change. The application
must also verify that the reservation cancellation or change succeeded, for example by checking the
number of affected rows.

## Changing a reservation

A reservation change must be performed in the following order within one transaction:

1. Begin the transaction.
2. Create the slot for the new date and hour.
3. Increment the new slot's count and verify that exactly one row was affected.
4. Decrement the old slot's count.
5. Update the reservation's date and hour.
6. Commit the transaction.
