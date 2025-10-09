package com.back.domain.notification.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = true)
    private String relatedUrl;

    @Column(nullable = false)
    private Boolean isRead;

    @Builder
    public Notification(User receiver, NotificationType type, String message, String relatedUrl) {
        this.receiver = receiver;
        this.type = type;
        this.message = message;
        this.relatedUrl = relatedUrl;
        this.isRead = false;
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 알림 읽지 않음 처리
     */
    public void markAsUnread() {
        this.isRead = false;
    }
}
