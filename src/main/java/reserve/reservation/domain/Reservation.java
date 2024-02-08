package reserve.reservation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import reserve.global.entity.BaseEntity;
import reserve.store.domain.Store;
import reserve.user.domain.User;

import java.time.LocalDate;

@Entity
@Table(name = "reservations")
@SQLRestriction("status = 'AVAILABLE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Column(nullable = false)
    @Setter
    private LocalDate date;

    @Column(columnDefinition = "TINYINT", nullable = false)
    @Setter
    private int hour;

    public Reservation(User user, Store store, LocalDate date, int hour) {
        this.user = user;
        this.store = store;
        this.date = date;
        this.hour = hour;
    }

}
