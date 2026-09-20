package reserve.store.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import reserve.global.entity.DeletableBaseEntity;
import reserve.user.domain.User;

import java.util.Objects;

@Entity
@Table(name = "stores")
@SQLRestriction("status = 'AVAILABLE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Store extends DeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false)
    @Setter
    private String address;

    @Column(nullable = false)
    @Setter
    private String description;

    /** `-1` means that the store's each slot can have unlimited reservations. */
    @Column(nullable = false)
    private Integer capacity = -1;

    public Store(User user, String name, String address, String description) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.description = description;
    }

    public Store(User user, String name, String address, String description, Integer capacity) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.description = description;
        this.capacity = Objects.requireNonNullElse(capacity, -1);
    }

    public void setCapacity(Integer capacity) {
        if (Objects.isNull(capacity) || capacity < -1) {
            throw new IllegalArgumentException("Capacity must be at least -1.");
        }
        this.capacity = capacity;
    }

}
