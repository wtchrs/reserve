package reserve.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.notification.service.NotificationService;
import reserve.reservation.domain.Reservation;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.infrastructure.ReservationQueryRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long userId, ReservationCreateRequest reservationCreateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO);
        }
        if (!storeRepository.existsById(reservationCreateRequest.getStoreId())) {
            throw new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND);
        }
        Reservation reservation = reservationRepository.save(new Reservation(
                userRepository.getReferenceById(userId),
                storeRepository.getReferenceById(reservationCreateRequest.getStoreId()),
                reservationCreateRequest.getDate(),
                reservationCreateRequest.getHour()
        ));
        return reservation.getId();
    }

    @Transactional(readOnly = true)
    public ReservationInfoResponse getReservationInfo(Long userId, Long reservationId) {
        return reservationRepository.findResponseByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ReservationInfoListResponse search(
            Long userId,
            ReservationSearchRequest reservationSearchRequest,
            Pageable pageable
    ) {
        if (!userRepository.existsById(userId)) {
            throw new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO);
        }
        Page<ReservationInfoResponse> result =
                reservationQueryRepository.findResponsesBySearch(userId, reservationSearchRequest, pageable);
        return ReservationInfoListResponse.from(result);
    }

    @Transactional
    public void update(Long userId, Long reservationId, ReservationUpdateRequest reservationUpdateRequest) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setDate(reservationUpdateRequest.getDate());
        reservation.setHour(reservationUpdateRequest.getHour());
    }

    @Transactional
    public void delete(Long userId, Long reservationId) {
        if (!reservationQueryRepository.existsByIdAndUserId(reservationId, userId)) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        reservationRepository.deleteById(reservationId);
    }

}
