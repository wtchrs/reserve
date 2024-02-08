package reserve.store.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.response.StoreInfoResponse;

import java.util.List;

import static reserve.store.domain.QStore.*;

@Repository
public class StoreQueryRepository {

    private final double matchThreshold;

    private final JPAQueryFactory queryFactory;

    public StoreQueryRepository(
            @Value("${application.matchThreshold}") double matchThreshold,
            EntityManager em
    ) {
        this.matchThreshold = matchThreshold;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<StoreInfoResponse> findResponsesBySearch(StoreSearchRequest storeSearchRequest, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(registrantUsernameCondition(storeSearchRequest.getRegistrant()));
        condition.and(queryStringCondition(storeSearchRequest.getQuery()));

        List<StoreInfoResponse> content = queryFactory
                .select(getStoreInfoResponseProjection())
                .from(store)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(store.count())
                .from(store)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }

    private static ConstructorExpression<StoreInfoResponse> getStoreInfoResponseProjection() {
        return Projections.constructor(
                StoreInfoResponse.class,
                store.id,
                store.user.username,
                store.name,
                store.price,
                store.address,
                store.description
        );
    }

    private static BooleanExpression registrantUsernameCondition(String registrant) {
        if (StringUtils.hasText(registrant)) {
            return store.user.username.eq(registrant);
        }
        return null;
    }

    private BooleanExpression queryStringCondition(String query) {
        if (StringUtils.hasText(query)) {
            return Expressions
                    .numberTemplate(Double.class, "fulltext_search(name, address, description, {0})", query)
                    .gt(matchThreshold);
        }
        return null;
    }

}
