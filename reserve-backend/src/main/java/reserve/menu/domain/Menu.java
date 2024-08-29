package reserve.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import reserve.global.entity.DeletableBaseEntity;
import reserve.store.domain.Store;

@Entity
@Table(name = "menus")
@SQLRestriction("status = 'AVAILABLE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Menu extends DeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false)
    @Setter
    private int price;

    @Column(nullable = false)
    @Setter
    private String description;

    public Menu(Store store, String name, int price, String description) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.description = description;
    }

}
