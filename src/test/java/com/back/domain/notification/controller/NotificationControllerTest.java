package com.back.domain.notification.controller;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("알림 컨트롤러 테스트")
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 조회 (TestInitData에서 생성된 user1@user.com 사용)
        testUser = userRepository.findByEmail("user1@user.com")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 알림만 삭제
        notificationRepository.deleteAll();
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("GET /api/notifications - 알림 목록 조회 성공")
    void getNotifications_Success() throws Exception {
        // given - 알림 생성
        notificationService.sendNotification(
                testUser,
                NotificationType.ORDER_CONFIRMED,
                "주문이 확정되었습니다.",
                "/mypage/orders/1"
        );

        // when & then
        mvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("알림 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("GET /api/notifications/unread-count - 읽지 않은 알림 개수 조회 성공")
    void getUnreadCount_Success() throws Exception {
        // given - 알림 3개 생성
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, "메시지1", "/url1");
        notificationService.sendNotification(testUser, NotificationType.SHIPPING_STARTED, "메시지2", "/url2");
        notificationService.sendNotification(testUser, NotificationType.DELIVERY_COMPLETED, "메시지3", "/url3");

        // when & then
        mvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("읽지 않은 알림 개수 조회 성공"))
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(3)));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @Transactional
    @DisplayName("PATCH /api/notifications/{id}/read - 알림 읽음 처리 성공")
    void markAsRead_Success() throws Exception {
        // given - 고유한 메시지로 알림 생성
        String uniqueMessage = "읽음테스트메시지_" + System.currentTimeMillis();
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, uniqueMessage, "/url");
        
        // 방금 생성한 알림 조회
        Notification notification = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiver().getId().equals(testUser.getId()))
                .filter(n -> n.getMessage().equals(uniqueMessage))
                .filter(n -> !n.getIsRead())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("생성한 알림을 찾을 수 없습니다."));

        // when & then
        mvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("알림 읽음 처리 완료"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("PATCH /api/notifications/read-all - 모든 알림 읽음 처리 성공")
    void markAllAsRead_Success() throws Exception {
        // given - 알림 3개 생성
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, "메시지1", "/url1");
        notificationService.sendNotification(testUser, NotificationType.SHIPPING_STARTED, "메시지2", "/url2");
        notificationService.sendNotification(testUser, NotificationType.DELIVERY_COMPLETED, "메시지3", "/url3");

        // when & then
        mvc.perform(patch("/api/notifications/read-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(3)));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @Transactional
    @DisplayName("DELETE /api/notifications/{id} - 알림 삭제 성공")
    void deleteNotification_Success() throws Exception {
        // given - 고유한 메시지로 알림 생성
        String uniqueMessage = "삭제테스트메시지_" + System.currentTimeMillis();
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, uniqueMessage, "/url");
        
        // 방금 생성한 알림 조회
        Notification notification = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiver().getId().equals(testUser.getId()))
                .filter(n -> n.getMessage().equals(uniqueMessage))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("생성한 알림을 찾을 수 없습니다."));

        // when & then
        mvc.perform(delete("/api/notifications/{id}", notification.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("알림 삭제 완료"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("GET /api/notifications - 페이징 검증")
    void getNotifications_Paging() throws Exception {
        // given - 알림 5개 생성
        for (int i = 1; i <= 5; i++) {
            notificationService.sendNotification(
                    testUser,
                    NotificationType.ORDER_CONFIRMED,
                    "메시지" + i,
                    "/url" + i
            );
        }

        // when & then
        mvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(3))
                .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    @DisplayName("GET /api/notifications - 인증 없이 접근 시 401")
    void getNotifications_Unauthorized() throws Exception {
        // when & then - 인증 없이 접근
        mvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
