package reserve.global.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor
@Getter
public class DeletableBaseEntity extends BaseEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusType status = StatusType.AVAILABLE;

    public boolean isDeleted() {
        return status.equals(StatusType.DELETED);
    }

    public void delete() {
        status = StatusType.DELETED;
    }

}
