package reserve.user.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reserve.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Modifying
    @Query("""
            UPDATE User user
            SET user.status = 'DELETED'
            WHERE user.id = :userId
            """)
    void deleteById(@Param("userId") Long id);

}
