package reserve.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.room.domain.Room;
import reserve.room.dto.request.RoomCreateRequest;
import reserve.room.dto.request.RoomSearchRequest;
import reserve.room.dto.request.RoomUpdateRequest;
import reserve.room.dto.response.RoomInfoListResponse;
import reserve.room.dto.response.RoomInfoResponse;
import reserve.room.infrastructure.RoomQueryRepository;
import reserve.room.infrastructure.RoomRepository;
import reserve.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomQueryRepository roomQueryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long userId, RoomCreateRequest roomCreateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO);
        }
        Room room = roomRepository.save(new Room(
                userRepository.getReferenceById(userId),
                roomCreateRequest.getName(),
                roomCreateRequest.getPrice(),
                roomCreateRequest.getAddress(),
                roomCreateRequest.getDescription()
        ));
        return room.getId();
    }

    @Transactional(readOnly = true)
    public RoomInfoResponse getRoomInfo(Long roomId) {
        return roomRepository.findResponseById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROOM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public RoomInfoListResponse search(RoomSearchRequest roomSearchRequest, Pageable pageable) {
        Page<RoomInfoResponse> page = roomQueryRepository.findResponsesBySearch(roomSearchRequest, pageable);
        return RoomInfoListResponse.from(page);
    }

    @Transactional
    public void update(Long userId, Long roomId, RoomUpdateRequest roomUpdateRequest) {
        Room room = roomRepository.findByIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROOM_NOT_FOUND));
        if (StringUtils.hasText(roomUpdateRequest.getName())) {
            room.setName(roomUpdateRequest.getName());
        }
        if (roomUpdateRequest.getPrice() != null) {
            room.setPrice(roomUpdateRequest.getPrice());
        }
        if (StringUtils.hasText(roomUpdateRequest.getAddress())) {
            room.setAddress(roomUpdateRequest.getAddress());
        }
        if (StringUtils.hasText(roomUpdateRequest.getDescription())) {
            room.setAddress(roomUpdateRequest.getDescription());
        }
    }

    @Transactional
    public void delete(Long userId, Long roomId) {
        if (!roomRepository.existsByIdAndUserId(roomId, userId)) {
            throw new ResourceNotFoundException(ErrorCode.ROOM_NOT_FOUND);
        }
        roomRepository.deleteById(roomId);
    }

}
