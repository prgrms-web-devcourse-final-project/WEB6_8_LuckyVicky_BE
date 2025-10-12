package com.back.domain.support.inquiry.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 1:1 문의 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiries")
public class Inquiry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InquiryCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.PENDING; // 문의 상태

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Boolean isSecret = false;  // 비밀문의 여부

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryDocument> documents = new ArrayList<>();  // 첨부파일 (최대 3개)

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryReply> replies = new ArrayList<>();  // 댓글 목록

    @Builder
    public Inquiry(User user, String title, String content, InquiryCategory category, Boolean isSecret) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category;
        this.isSecret = isSecret != null ? isSecret : false;
        this.status = InquiryStatus.PENDING;
        this.viewCount = 0L;
    }

    /**
     * 문의 수정
     */
    public void update(String title, String content, InquiryCategory category, Boolean isSecret) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (category != null) this.category = category;
        if (isSecret != null) this.isSecret = isSecret;
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 댓글 추가
     */
    public void addReply(InquiryReply reply) {
        this.replies.add(reply);
    }

    /**
     * 첨부파일 추가
     */
    public void addDocument(InquiryDocument document) {
        this.documents.add(document);
    }

    /**
     * 문의 상태 변경
     */
    public void changeStatus(InquiryStatus status) {
        this.status = status;
    }

    /**
     * 첨부파일 전체 삭제
     */
    public void clearDocuments() {
        this.documents.clear();
    }

    /**
     * 특정 사용자가 작성한 문의인지 확인
     */
    public boolean isWrittenBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    /**
     * 비밀문의인지 확인
     */
    public boolean isSecret() {
        return this.isSecret;
    }

    /**
     * 비밀문의 접근 권한 확인
     */
    public boolean canAccess(Long userId, boolean isAdmin) {
        // 관리자는 모든 문의 접근 가능
        if (isAdmin) {
            return true;
        }

        // 비밀문의가 아니면 모두 접근 가능
        if (!this.isSecret) {
            return true;
        }

        // 비밀문의는 작성자만 접근 가능
        return this.isWrittenBy(userId);
    }
}
