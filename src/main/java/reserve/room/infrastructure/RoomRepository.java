package reserve.room.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reserve.room.domain.Room;
import reserve.room.dto.response.RoomInfoResponse;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByIdAndUserId(Long roomId, Long userId);

    Optional<Room> findByIdAndUserId(Long roomId, Long userId);

    @Query("""
           SELECT new reserve.room.dto.response.RoomInfoResponse(
               room.id, room.user.username, room.name, room.price, room.address, room.description
           )
           FROM Room room
           WHERE room.id = :roomId
           """)
    Optional<RoomInfoResponse> findResponseById(@Param("roomId") Long roomId);

    @Modifying
    @Query(
            """
            UPDATE Room room
            SET room.status = 'DELETED'
            WHERE room.id = :roomId
            """)
    void deleteById(@Param("roomId") Long roomId);

}
