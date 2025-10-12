package com.back.domain.support.inquiry.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 1:1 문의 답변 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry_replies")
public class InquiryReply extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReplyType replyType; // 답변 유형 (ADMIN, USER)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id")
    private InquiryReply parentReply; // 대댓글의 경우 부모 댓글

    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryReply> childReplies = new ArrayList<>(); // 대댓글 목록

    @Builder
    public InquiryReply(Inquiry inquiry, User user, String content, ReplyType replyType, InquiryReply parentReply) {
        this.inquiry = inquiry;
        this.user = user;
        this.content = content;
        this.replyType = replyType;
        this.parentReply = parentReply;
    }

    /**
     * 댓글 수정
     */
    public void update(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    /**
     * 관리자 답변인지 확인
     */
    public boolean isAdminReply() {
        return this.replyType == ReplyType.ADMIN;
    }

    /**
     * 특정 사용자가 작성한 댓글인지 확인
     */
    public boolean isWrittenBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    /**
     * 대댓글인지 확인
     */
    public boolean isChildReply() {
        return this.parentReply != null;
    }

    /**
     * 자식 댓글 추가 (대댓글)
     */
    public void addChildReply(InquiryReply childReply) {
        this.childReplies.add(childReply);
    }
}
