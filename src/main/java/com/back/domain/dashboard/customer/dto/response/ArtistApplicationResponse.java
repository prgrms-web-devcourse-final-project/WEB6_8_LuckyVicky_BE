package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 작가 신청 관련 응답 DTO
 * 
 * 작가 입점 신청과 관련된 모든 응답 데이터를 포함
 * 신청 목록 조회, 상세 조회 등에서 사용
 * 2025.09.22 수정
 */
public class ArtistApplicationResponse {
    
    /**
     * 작가 신청 목록 응답
     * 페이징된 작가 신청 목록과 요약 정보를 포함
     */
    @Getter
    @Setter
    public static class List extends PageResponse<ArtistApplicationResponse.Summary> {
        /** 신청 현황 요약 정보 */
        private SummaryDto summary;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Summary> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
    }
    
    /**
     * 작가 신청 상세 정보
     * 신청서의 모든 상세 정보를 포함
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        /** 신청 기본 정보 */
        private Application application;
        /** 신청자 정보 */
        private Applicant applicant;
        /** 연락처 정보 */
        private Contact contact;
        /** 사업자 정보 */
        private Business business;
        /** 작가 프로필 정보 */
        private Profile profile;
        /** 권한 정보 */
        private Permission permissions;
    }
    
    /**
     * 신청 현황 요약 정보
     *전체, 대기중, 승인, 거절 건수를 포함
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        /** 전체 신청 건수 */
        private int total;
        /** 대기중인 신청 건수 */
        private int pending;
        /** 승인된 신청 건수 */
        private int approved;
        /** 거절된 신청 건수 */
        private int rejected;
    }
    
    /**
     * 작가 신청 요약 정보
     * 목록에서 표시되는 간략한 신청 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /** 신청 ID */
        private Long applicationId;
        /** 작가명 */
        private String artistName;
        /** 신청일 (yyyy-MM-dd) */
        private String submittedAt;
        /** 신청 상태 (PENDING/APPROVED/REJECTED) */
        private String status;
        /** 상태 텍스트 */
        private String statusText;
        /** 권한 정보 */
        private Permission permissions;
        /** 마지막 업데이트 일시 */
        private LocalDateTime lastUpdatedAt;
    }
    
    /**
     * 신청 기본 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Application {
        private Long applicationId;
        private String status;
        private String statusText;
        private LocalDateTime submittedAt;
        private LocalDateTime decidedAt;
        private String rejectionReason;
        /** 검토자 정보 (선택적) */
        private Reviewer reviewer;
    }
    
    /**
     * 신청자 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Applicant {
        private String memberId;
        private String artistName;
        private String avatarUrl;
    }
    
    /**
     * 연락처 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private String email;
        private String phone;
    }
    
    /**
     * 사업자 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Business {
        private String registrationNo;
        private FileDto registrationProof;
        private String telemarketingReportNo;
        private FileDto telemarketingReportProof;
        private String address;
    }
    
    /**
     * 작가 프로필 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String description;
        private java.util.List<String> mainCategories;
        private java.util.List<Sns> sns;
        private java.util.List<FileDto> portfolio;
    }
    
    /**
     * 첨부 파일 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDto {
        private String fileId;
        private String fileName;
        private String url;
        private LocalDateTime expiresAt;
    }
    
    /**
     * SNS 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sns {
        private String platform;
        private String handle;
    }
    
    /**
     * 권한 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private Boolean canEdit;
        private Boolean canCancel;
        /** 재신청/이의제기 가능 여부 (선택적) */
        private Boolean canAppeal;
    }
    
    /**
     * 검토자 정보 (선택적)
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reviewer {
        /** 관리자 ID */
        private String id;
        /** 관리자명 */
        private String name;
    }
}
