package reserve.menu.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.menu.domain.Menu;

import java.util.Optional;

import static reserve.menu.domain.QMenu.*;

@Repository
public class MenuQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MenuQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public boolean hasAccessToMenu(Long menuId, Long userId) {
        Boolean result = queryFactory.select(menu.store.user.id.eq(userId))
                .from(menu)
                .where(menu.id.eq(menuId))
                .fetchOne();
        if (result == null) {
            throw new ResourceNotFoundException(ErrorCode.MENU_NOT_FOUND);
        }
        return result;
    }

}
