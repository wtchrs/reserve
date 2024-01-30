package reserve.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reserve.user.domain.User;

import java.util.Optional;

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
