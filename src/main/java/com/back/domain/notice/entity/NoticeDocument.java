package com.back.domain.notice.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.global.s3.S3FileRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 첨부파일 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice_documents")
public class NoticeDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String fileUrl; // S3 URL

    @Column(nullable = false, length = 500)
    private String s3Key; // S3 Key

    @Builder
    public NoticeDocument(Notice notice, String fileName, String fileUrl, String s3Key) {
        this.notice = notice;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.s3Key = s3Key;
    }

    /**
     * S3FileRequest로부터 NoticeDocument 엔티티 생성
     * @param notice 공지사항
     * @param s3FileRequest S3 파일 업로드 정보
     * @return 생성된 NoticeDocument 엔티티
     */
    public static NoticeDocument fromS3FileRequest(Notice notice, S3FileRequest s3FileRequest) {
        return NoticeDocument.builder()
                .notice(notice)
                .fileName(s3FileRequest.originalFileName())
                .fileUrl(s3FileRequest.url())
                .s3Key(s3FileRequest.s3Key())
                .build();
    }

    /**
     * 공지사항 설정 (양방향 관계)
     */
    void setNotice(Notice notice) {
        this.notice = notice;
    }

    /**
     * 첨부파일이 특정 공지사항에 속하는지 확인
     */
    public boolean belongsTo(Long noticeId) {
        return this.notice.getId().equals(noticeId);
    }

}
