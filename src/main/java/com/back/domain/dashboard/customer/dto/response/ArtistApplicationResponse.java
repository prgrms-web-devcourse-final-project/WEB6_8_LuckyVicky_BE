package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

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
    public static class List extends PageResponse<ArtistApplicationResponse.Summary> {
        /** 신청 현황 요약 정보 */
        private final SummaryDto summary;
        
        public List() {
            super();
            this.summary = null;
        }
        
        public List(SummaryDto summary, java.util.List<Summary> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
        
        public SummaryDto getSummary() {
            return summary;
        }
    }
    
    /**
     * 작가 신청 상세 정보
     * 신청서의 모든 상세 정보를 포함
     */
    public record Detail(
            /** 신청 기본 정보 */
            Application application,
            /** 신청자 정보 */
            Applicant applicant,
            /** 연락처 정보 */
            Contact contact,
            /** 사업자 정보 */
            Business business,
            /** 작가 프로필 정보 */
            Profile profile,
            /** 권한 정보 */
            Permission permissions
    ) {}
    
    /**
     * 신청 현황 요약 정보
     *전체, 대기중, 승인, 거절 건수를 포함
     */
    public record SummaryDto(
            /** 전체 신청 건수 */
            int total,
            /** 대기중인 신청 건수 */
            int pending,
            /** 승인된 신청 건수 */
            int approved,
            /** 거절된 신청 건수 */
            int rejected
    ) {}
    
    /**
     * 작가 신청 요약 정보
     * 목록에서 표시되는 간략한 신청 정보
     */
    public record Summary(
            /** 신청 ID */
            Long applicationId,
            /** 작가명 */
            String artistName,
            /** 신청일 (yyyy-MM-dd) */
            String submittedAt,
            /** 신청 상태 (PENDING/APPROVED/REJECTED) */
            String status,
            /** 상태 텍스트 */
            String statusText,
            /** 권한 정보 */
            Permission permissions,
            /** 마지막 업데이트 일시 */
            LocalDateTime lastUpdatedAt
    ) {}
    
    /**
     * 신청 기본 정보
     */
    public record Application(
            Long applicationId,
            String status,
            String statusText,
            LocalDateTime submittedAt,
            LocalDateTime decidedAt,
            String rejectionReason,
            /** 검토자 정보 (선택적) */
            Reviewer reviewer
    ) {}
    
    /**
     * 신청자 정보
     */
    public record Applicant(
            String memberId,
            String artistName,
            String avatarUrl
    ) {}
    
    /**
     * 연락처 정보
     */
    public record Contact(
            String email,
            String phone
    ) {}
    
    /**
     * 사업자 정보
     */
    public record Business(
            String registrationNo,
            FileDto registrationProof,
            String telemarketingReportNo,
            FileDto telemarketingReportProof,
            String address
    ) {}
    
    /**
     * 작가 프로필 정보
     */
    public record Profile(
            String description,
            java.util.List<String> mainCategories,
            java.util.List<Sns> sns,
            java.util.List<FileDto> portfolio
    ) {}
    
    /**
     * 첨부 파일 정보
     */
    public record FileDto(
            String fileId,
            String fileName,
            String url,
            LocalDateTime expiresAt
    ) {}
    
    /**
     * SNS 정보
     */
    public record Sns(
            String platform,
            String handle
    ) {}
    
    /**
     * 권한 정보
     */
    public record Permission(
            Boolean canEdit,
            Boolean canCancel,
            /** 재신청/이의제기 가능 여부 (선택적) */
            Boolean canAppeal
    ) {}
    
    /**
     * 검토자 정보 (선택적)
     */
    public record Reviewer(
            /** 관리자 ID */
            String id,
            /** 관리자명 */
            String name
    ) {}
}
