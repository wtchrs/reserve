package reserve.room.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import reserve.global.entity.BaseEntity;
import reserve.user.domain.User;

@Entity
@Table(name = "rooms")
@SQLRestriction("status = 'AVAILABLE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false)
    @Setter
    private int price;

    @Column(nullable = false)
    @Setter
    private String address;

    @Column(nullable = false)
    @Setter
    private String description;

    public Room(User user, String name, int price, String address, String description) {
        this.user = user;
        this.name = name;
        this.price = price;
        this.address = address;
        this.description = description;
    }

}
