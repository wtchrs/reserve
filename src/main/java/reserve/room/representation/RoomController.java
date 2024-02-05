package reserve.room.representation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.room.dto.request.RoomCreateRequest;
import reserve.room.dto.request.RoomSearchRequest;
import reserve.room.dto.request.RoomUpdateRequest;
import reserve.room.dto.response.RoomInfoListResponse;
import reserve.room.dto.response.RoomInfoResponse;
import reserve.room.service.RoomService;

import java.net.URI;

@RestController
@RequestMapping("/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Void> create(
            @Authentication AuthInfo authInfo,
            @RequestBody @Validated RoomCreateRequest roomCreateRequest
    ) {
        Long roomId = roomService.create(authInfo.getUserId(), roomCreateRequest);
        return ResponseEntity.created(URI.create("/v1/rooms/" + roomId)).build();
    }

    @GetMapping("/{roomId}")
    public RoomInfoResponse getRoomInfo(@PathVariable("roomId") Long roomId) {
        return roomService.getRoomInfo(roomId);
    }

    @GetMapping
    public RoomInfoListResponse search(
            @ModelAttribute @Validated RoomSearchRequest roomSearchRequest,
            Pageable pageable
    ) {
        return roomService.search(roomSearchRequest, pageable);
    }

    @PutMapping("/{roomId}")
    public void update(
            @Authentication AuthInfo authInfo,
            @PathVariable("roomId") Long roomId,
            @RequestBody @Validated RoomUpdateRequest roomUpdateRequest
    ) {
        roomService.update(authInfo.getUserId(), roomId, roomUpdateRequest);
    }

    @DeleteMapping("/{roomId}")
    public void delete(@Authentication AuthInfo authInfo, @PathVariable("roomId") Long roomId) {
        roomService.delete(authInfo.getUserId(), roomId);
    }

}
