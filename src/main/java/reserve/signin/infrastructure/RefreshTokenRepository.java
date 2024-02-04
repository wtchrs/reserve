package reserve.signin.infrastructure;

import org.springframework.data.repository.CrudRepository;
import reserve.signin.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
