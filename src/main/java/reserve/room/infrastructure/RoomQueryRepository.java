package reserve.room.infrastructure;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reserve.room.dto.request.RoomSearchRequest;
import reserve.room.dto.response.RoomInfoResponse;

import java.util.List;

import static reserve.room.domain.QRoom.*;

@Repository
public class RoomQueryRepository {

    private final JPAQueryFactory queryFactory;

    public RoomQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<RoomInfoResponse> findResponsesBySearch(RoomSearchRequest roomSearchRequest, Pageable pageable) {
        List<RoomInfoResponse> content = queryFactory
                .select(getRoomInfoResponseProjection())
                .from(room)
                .where(
                        registrantUsernameCondition(roomSearchRequest.getRegistrant()),
                        queryStringCondition(roomSearchRequest.getQuery())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(room.count())
                .from(room)
                .where(
                        registrantUsernameCondition(roomSearchRequest.getRegistrant()),
                        queryStringCondition(roomSearchRequest.getQuery())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }

    private static ConstructorExpression<RoomInfoResponse> getRoomInfoResponseProjection() {
        return Projections.constructor(
                RoomInfoResponse.class,
                room.id,
                room.user.username,
                room.name,
                room.price,
                room.address,
                room.description
        );
    }

    private static BooleanExpression registrantUsernameCondition(String registrant) {
        if (StringUtils.hasText(registrant)) {
            return room.user.username.eq(registrant);
        }
        return null;
    }

    private BooleanExpression queryStringCondition(String query) {
        if (StringUtils.hasText(query)) {
            // TODO: Replace Full-text search
            return room.name.like("%" + query + "%")
                    .or(room.address.like("%" + query + "%"))
                    .or(room.description.like("%" + query + "%"));
        }
        return null;
    }

}
