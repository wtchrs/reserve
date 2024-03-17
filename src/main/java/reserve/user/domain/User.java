package reserve.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import reserve.global.entity.DeletableBaseEntity;

@Entity
@Table(name = "users")
@SQLRestriction("status = 'AVAILABLE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends DeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Setter
    private String nickname;

    @Setter
    private String description;

    public User(String username, String passwordHash, String nickname, String description) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.description = description;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

}
