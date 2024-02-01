package reserve.room.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reserve.global.entity.BaseEntity;
import reserve.user.domain.User;

@Entity
@Table(name = "rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Room extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String description;

}
