package reserve.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reserve.reservation.domain.Reservation;
import reserve.reservation.dto.response.ReservationInfoResponse;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.id = :reservationId and r.user.id = :userId")
    Optional<Reservation> findByIdAndUserId(@Param("reservationId") Long reservationId, @Param("userId") Long userId);

    @Query("SELECT r.store.user.id FROM Reservation r WHERE r.id = :reservationId")
    Optional<Long> findStoreUserIdById(@Param("reservationId") Long reservationId);

    @Query("""
           SELECT new reserve.reservation.dto.response.ReservationInfoResponse(
               r.id, r.store.id, r.store.user.username, r.user.username, r.date, r.hour
           )
           FROM Reservation r
           WHERE r.id = :reservationId and (r.user.id = :userId or r.store.user.id = :userId)
           """)
    Optional<ReservationInfoResponse> findResponseByIdAndUserId(
            @Param("reservationId") Long reservationId,
            @Param("userId") Long userId
    );

}
