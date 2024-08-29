package reserve.reservation.infrastructure;

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
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.reservation.dto.ReservationForNotifyDto;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.response.ReservationInfoResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static reserve.reservation.domain.QReservation.*;

@Repository
public class ReservationQueryRepository {

    private final double matchThreshold;

    private final JPAQueryFactory queryFactory;

    public ReservationQueryRepository(@Value("${application.matchThreshold}") double matchThreshold, EntityManager em) {
        this.matchThreshold = matchThreshold;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public boolean existsByIdAndUserId(Long reservationId, Long userId) {
        Integer result = queryFactory.selectOne()
                .from(reservation)
                .where(
                        reservation.id.eq(reservationId),
                        reservation.user.id.eq(userId).or(reservation.store.user.id.eq(userId))
                )
                .fetchFirst();

        return result != null && result == 1;
    }

    public boolean hasReadAccessToReservation(Long reservationId, Long userId) {
        Boolean result = queryFactory
                .select(reservation.store.user.id.eq(userId).or(reservation.user.id.eq(userId)))
                .from(reservation)
                .where(reservation.id.eq(reservationId))
                .fetchOne();

        if (result == null) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return result;
    }

    public Optional<ReservationForNotifyDto> findForNotifyById(Long reservationId) {
        ReservationForNotifyDto result = queryFactory
                .select(
                        Projections.constructor(
                                ReservationForNotifyDto.class,
                                reservation.id,
                                reservation.user.id,
                                reservation.store.user.id
                        )
                )
                .from(reservation)
                .where(reservation.id.eq(reservationId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public Page<ReservationInfoResponse> findResponsesBySearch(
            Long userId, ReservationSearchRequest reservationSearchRequest,
            Pageable pageable
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(registrantOrCustomerCondition(reservationSearchRequest.getType(), userId));
        condition.and(storeQueryCondition(reservationSearchRequest.getQuery()));
        condition.and(dateCondition(reservationSearchRequest.getDate()));

        List<ReservationInfoResponse> result = queryFactory.select(getReservationInfoResponseProjection())
                .from(reservation)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory.select(reservation.count())
                .from(reservation)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(result, pageable, count);
    }

    private static ConstructorExpression<ReservationInfoResponse> getReservationInfoResponseProjection() {
        return Projections.constructor(
                ReservationInfoResponse.class,
                reservation.id,
                reservation.store.id,
                reservation.store.user.username,
                reservation.user.username,
                reservation.date,
                reservation.hour
        );
    }

    private static BooleanExpression registrantOrCustomerCondition(
            ReservationSearchRequest.SearchType type,
            Long userId
    ) {
        if (type.equals(ReservationSearchRequest.SearchType.REGISTRANT)) {
            return reservation.store.user.id.eq(userId);
        } else {
            return reservation.user.id.eq(userId);
        }
    }

    private BooleanExpression storeQueryCondition(String storeQuery) {
        if (StringUtils.hasText(storeQuery)) {
            return Expressions.numberTemplate(
                    Double.class,
                    "fulltext_search(store.name, store.address, store.description, {0})",
                    storeQuery
            ).gt(matchThreshold);
        }
        return null;
    }

    private static BooleanExpression dateCondition(LocalDate date) {
        if (date != null) {
            return reservation.date.eq(date);
        }
        return null;
    }

}
