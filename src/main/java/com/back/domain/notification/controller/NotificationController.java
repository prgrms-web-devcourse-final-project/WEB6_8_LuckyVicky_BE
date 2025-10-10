package com.back.domain.notification.controller;

import com.back.domain.notification.dto.NotificationResponse;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.global.util.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 컨트롤러
 * 
 * TODO: 미구현 알림 타입 (추후 구현 예정)
 * 문의하신 질문에 답변이 달렸습니다 (qna 도메인 구현 후)
 * 새로운 질문이 등록되었습니다 (qna 도메인 구현 후)
 * 새로운 리뷰가 등록되었습니다 (review 도메인 구현 후)
 * 정산금이 입금되었습니다 (settlement 도메인 구현 후)
 * 정산 요청 (settlement 도메인 구현 후)
 */
@Tag(name = "Notification", description = "알림 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final Rq rq;

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이징하여 조회합니다.")
    @GetMapping
    public RsData<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = rq.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<NotificationResponse> notifications = notificationService.getNotifications(user, pageable);

        return RsData.of(
                "200",
                "알림 목록 조회 성공",
                PageResponse.from(notifications)
        );
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public RsData<Long> getUnreadCount() {
        User user = rq.getUser();
        long count = notificationService.getUnreadCount(user);

        return RsData.of(
                "200",
                "읽지 않은 알림 개수 조회 성공",
                count
        );
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{notificationId}/read")
    public RsData<Void> markAsRead(@PathVariable Long notificationId) {
        User user = rq.getUser();
        notificationService.markAsRead(user, notificationId);

        return RsData.of(
                "200",
                "알림 읽음 처리 완료",
                null
        );
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public RsData<Integer> markAllAsRead() {
        User user = rq.getUser();
        int count = notificationService.markAllAsRead(user);

        return RsData.of(
                "200",
                count + "개의 알림을 읽음 처리했습니다.",
                count
        );
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{notificationId}")
    public RsData<Void> deleteNotification(@PathVariable Long notificationId) {
        User user = rq.getUser();
        notificationService.deleteNotification(user, notificationId);

        return RsData.of(
                "200",
                "알림 삭제 완료",
                null
        );
    }
}
