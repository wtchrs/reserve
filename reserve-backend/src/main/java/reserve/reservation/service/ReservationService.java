package reserve.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.menu.domain.Menu;
import reserve.menu.infrastructure.MenuRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationMenu;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationMenuCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.*;
import reserve.reservation.infrastructure.ReservationMenuRepository;
import reserve.reservation.infrastructure.ReservationQueryRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.infrastructure.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final ReservationMenuRepository reservationMenuRepository;
    private final MenuRepository menuRepository;
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
        Map<Long, Menu> menuMap = getMenuMap(reservationCreateRequest);
        List<ReservationMenu> reservationMenuList = reservationCreateRequest.getMenus().stream()
                .map(req -> createReservationMenu(reservation, menuMap.get(req.getMenuId()), req.getQuantity()))
                .toList();
        reservationMenuRepository.saveAll(reservationMenuList);
        return reservation.getId();
    }

    private Map<Long, Menu> getMenuMap(ReservationCreateRequest reservationCreateRequest) {
        List<Long> menuIdList =
                reservationCreateRequest.getMenus().stream().map(ReservationMenuCreateRequest::getMenuId).toList();
        return menuRepository.findAllById(menuIdList).stream().collect(Collectors.toMap(Menu::getId, m -> m));
    }

    private ReservationMenu createReservationMenu(Reservation reservation, Menu menu, int quantity) {
        if (menu == null || !menu.getStore().getId().equals(reservation.getStore().getId())) {
            throw new ResourceNotFoundException(ErrorCode.MENU_NOT_FOUND);
        }
        return new ReservationMenu(reservation, menu.getName(), menu.getPrice(), quantity);
    }

    @Transactional(readOnly = true)
    public ReservationInfoResponse getReservationInfo(Long userId, Long reservationId) {
        return reservationRepository.findResponseByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Transactional
    public ReservationMenuListResponse getReservationMenus(Long userId, Long reservationId) {
        if (!reservationQueryRepository.hasReadAccessToReservation(reservationId, userId)) {
            throw new AuthenticationException(ErrorCode.ACCESS_DENIED);
        }
        List<ReservationMenuResponse> responses = reservationMenuRepository.findResponsesByReservationId(reservationId);
        return ReservationMenuListResponse.from(responses);
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
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setDate(reservationUpdateRequest.getDate());
        reservation.setHour(reservationUpdateRequest.getHour());
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        reservationRepository
                .findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND))
                .cancel();
    }

}
