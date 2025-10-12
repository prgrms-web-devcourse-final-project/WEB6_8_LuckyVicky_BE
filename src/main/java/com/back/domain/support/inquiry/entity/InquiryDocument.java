package com.back.domain.support.inquiry.entity;


import com.back.global.jpa.entity.BaseEntity;
import com.back.global.s3.S3FileRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1:1 문의 첨부파일 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry_documents")
public class InquiryDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String fileUrl;  // S3 URL

    @Column(nullable = false, length = 500)
    private String s3Key;  // S3 Key

    @Builder
    public InquiryDocument(Inquiry inquiry, String fileName, String fileUrl, String s3Key) {
        this.inquiry = inquiry;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.s3Key = s3Key;
    }

    /**
     * S3FileRequest로부터 InquiryDocument 엔티티 생성
     * @param inquiry 문의사항
     * @param s3FileRequest S3 파일 업로드 정보
     * @return 생성된 InquiryDocument 엔티티
     */
    public static InquiryDocument fromS3FileRequest(Inquiry inquiry, S3FileRequest s3FileRequest) {
        return InquiryDocument.builder()
                .inquiry(inquiry)
                .fileName(s3FileRequest.originalFileName())
                .fileUrl(s3FileRequest.url())
                .s3Key(s3FileRequest.s3Key())
                .build();
    }

    /**
     * 첨부파일이 특정 문의에 속하는지 확인
     */
    public boolean belongsTo(Long inquiryId) {
        return this.inquiry.getId().equals(inquiryId);
    }
}
