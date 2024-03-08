package reserve.reservation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reserve.global.entity.BaseEntity;

@Entity
@Table(name = "reservation_menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_menu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, updatable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int quantity;

    public ReservationMenu(Reservation reservation, String name, int price, int quantity) {
        this.reservation = reservation;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

}
