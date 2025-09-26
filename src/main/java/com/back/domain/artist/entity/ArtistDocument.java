package com.back.domain.artist.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.global.s3.S3FileRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작가 신청서 서류 엔티티
 */
@Getter
@Entity
@Table(name = "artist_documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_application_id", nullable = false)
    private ArtistApplication artistApplication;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String fileName;

    private String fileUrl; // S3 URL

    private String s3Key; // S3 Key

    @Builder
    public ArtistDocument(ArtistApplication artistApplication, DocumentType documentType, String fileName, String fileUrl, String s3Key) {
        this.artistApplication = artistApplication;
        this.documentType = documentType;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.s3Key = s3Key;
    }


    // ==== 생성 관련 메서드 ==== //

    /**
     * S3FileRequest로부터 ArtistDocument 엔티티 생성
     * @param artistApplication 작가 신청서
     * @param documentType 서류 타입(ex. 사업자등록증, 통신판매업신고증, 포트폴리오 등)
     * @param s3FileRequest S3 파일 업로드 덩보
     * @return 생성된 ArtistDocument 엔티티
     */
    public static ArtistDocument fromS3FileRequest(ArtistApplication artistApplication,
                                                   DocumentType documentType,
                                                   S3FileRequest s3FileRequest) {
        return ArtistDocument.builder()
                .artistApplication(artistApplication)
                .documentType(documentType)
                .fileName(s3FileRequest.originalFileName())
                .fileUrl(s3FileRequest.url())
                .s3Key(s3FileRequest.s3Key())
                .build();
    }


    // ==== 파일 정보 조회 메서드들 ==== //

    /**
     * 파일 확장자 추출
     * @return 소문자 확장자 (ex. "pdf", "jpg"), 확장자가 없으면 빈 문자열
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * PDF 파일 여부 확인
     * @return PDF 파일이면 true, 아니면 false
     */
    public boolean isPdfFile() {
        return "pdf".equals(getFileExtension());
    }

    /**
     * 이미지 파일 여부 확인
     * @return 이미지 파일이면 true, 아니면 false
     */
    public boolean isImageFile() {
        String ext = getFileExtension();
        return ext.matches("jpg|jpeg|png|gif|bmp|webp");
    }

    // ==== 유효성 검증 메서드들 ==== //

    /**
     * 서류 타입에 맞는 파일 형식인지 검증
     * @return 해당 서류 타입에 유효한 파일이면 true, 아니면 false
     */
    public boolean isValidForDocumentType() {
        switch (documentType) {
            case BUSINESS_LICENSE:          // 사업자등록증
            case TELECOM_CERTIFICATION:     // 통신판매업신고증
            case PORTFOLIO:                 // 포트폴리오
                return isPdfFile() || isImageFile(); // PDF 또는 이미지 파일 허용
            case OTHER:
                return true; // 기타는 모든 타입 허용
            default:
                return false;
        }
    }

    /**
     * 이 서류가 특정 작가 신청서에 속하는지 확인 - URL 파라미터 조작을 통한 타인 서류 접근 방지용
     * @param applicationId 확인할 신청서 Id
     * @return 해당 신청서에 속하면 true, 아니면 false
     */
    public boolean belongsTo(Long applicationId) {
        return this.artistApplication.getId().equals(applicationId);
    }
}
