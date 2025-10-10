package com.back.domain.notice.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 공지사항 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices")
public class Notice extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean isImportant = false; // 공지사항 중요 여부

    @Column(nullable = false)
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeDocument> documents = new ArrayList<>(); // 공지사항 첨부파일 목록

    @Builder
    public Notice(String title, String content, Boolean isImportant) {
        this.title = title;
        this.content = content;
        this.isImportant = isImportant != null ? isImportant : false;
        this.viewCount = 0L;
    }

    // ==== 비즈니스 로직 ==== //

    /**
     * 공지사항 수정
     */
    public void update(String title, String content, Boolean isImportant) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (isImportant != null) this.isImportant = isImportant;
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 첨부파일 추가
     */
    public void addDocument(NoticeDocument document) {
        this.documents.add(document);
        document.setNotice(this);
    }

    /**
     * 첨부파일 전체 삭제
     */
    public void clearAttachments() {
        this.documents.clear();
    }
}
