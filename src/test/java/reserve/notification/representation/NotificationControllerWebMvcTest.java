package reserve.notification.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import reserve.notification.domain.NotificationStatus;
import reserve.notification.domain.ResourceType;
import reserve.notification.dto.response.NotificationInfo;
import reserve.notification.dto.response.NotificationInfoListResponse;
import reserve.notification.service.NotificationService;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(JwtProvider.class)
class NotificationControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @MockBean
    NotificationService notificationService;

    @Test
    @DisplayName("Testing GET /v1/notifications endpoint")
    void testGetUserNotificationsEndpoint() throws Exception {
        Long userId = 1L;
        long resourceId = 100L;

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(userId));

        NotificationInfo notification1 = new NotificationInfo(
                userId,
                ResourceType.RESERVATION,
                resourceId,
                "message1",
                LocalDateTime.now(),
                NotificationStatus.UNREAD
        );
        NotificationInfo notification2 = new NotificationInfo(
                userId,
                ResourceType.RESERVATION,
                resourceId,
                "message2",
                LocalDateTime.now(),
                NotificationStatus.UNREAD
        );
        NotificationInfo notification3 = new NotificationInfo(
                userId,
                ResourceType.RESERVATION,
                resourceId,
                "message3",
                LocalDateTime.now(),
                NotificationStatus.UNREAD
        );

        NotificationInfoListResponse response = NotificationInfoListResponse.from(new PageImpl<>(
                List.of(notification3, notification2, notification1),
                PageRequest.of(0, 20),
                3
        ));

        Mockito.when(notificationService.getUserNotifications(Mockito.eq(userId), Mockito.any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(
                get("/v1/notifications").header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.count").value(3),
                jsonPath("$.results[2].message").value("message1"),
                jsonPath("$.results[1].message").value("message2"),
                jsonPath("$.results[0].message").value("message3")
        );
    }

    @Test
    @DisplayName("Testing POST /v1/notifications/{notificationId}/read endpoint")
    void testReadNotificationEndpoint() throws Exception {
        Long userId = 1L;
        Long notificationId = 1000L;

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(userId));

        mockMvc.perform(
                post("/v1/notifications/{notificationId}/read", notificationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(notificationService).readNotification(userId, notificationId);
    }

    @Test
    @DisplayName("Testing POST /v1/notifications/read-all endpoint")
    void testReadAllNotificationsEndpoint() throws Exception {
        Long userId = 1L;

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(userId));

        mockMvc.perform(
                post("/v1/notifications/read-all")
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(notificationService).readAllNotifications(userId);
    }

}
