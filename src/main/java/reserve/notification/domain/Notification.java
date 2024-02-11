package reserve.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reserve.global.entity.BaseEntity;
import reserve.reservation.domain.Reservation;
import reserve.user.domain.User;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @JoinColumn(nullable = false, updatable = false)
    private Long resourceId;

    @Column(nullable = false, updatable = false)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.UNREAD;

    public Notification(User user, ResourceType resourceType, Long resourceId, String message) {
        this.user = user;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.message = message;
    }

    public boolean isStatusRead() {
        return status.equals(NotificationStatus.READ);
    }

    public void read() {
        this.status = NotificationStatus.READ;
    }

}
