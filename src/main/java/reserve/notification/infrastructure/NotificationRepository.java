package reserve.notification.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import reserve.notification.domain.Notification;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdAndUserId(Long notificationId, Long userId);

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.user.id = :userId AND n.id = :notificationId")
    void setReadByUserIdAndId(Long userId, Long notificationId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.user.id = :userId")
    void setReadAllByUserId(Long userId);

}
