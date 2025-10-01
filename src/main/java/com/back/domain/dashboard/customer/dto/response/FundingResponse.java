package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import java.time.LocalDateTime;

/**
 * 펀딩 참여 관련 응답 DTO
 * 사용자가 참여한 펀딩들의 정보를 포함
 * 2025.09.30 수정 - Funding 엔티티 구조에 맞춰 타입 수정 (int → long, String id → Long id)
 */
public class FundingResponse {

    /**
     * 참여한 펀딩 목록 응답
     */
    public static class List extends PageResponse<FundingResponse.Participation> {
        /**
         * 펀딩 참여 현황 요약 정보
         */
        private final SummaryDto summary;

        public List() {
            super();
            this.summary = null;
        }

        public List(SummaryDto summary, java.util.List<Participation> content,
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
     * 2025.09.30 수정 - 배송 상태는 배송 CRUD 완성 후 활성화 예정
     */
    /** 프런트랑 상의후 아래 기능 적용 및 삭제 예정*/
    public record SummaryDto(
            /** 전체 참여 펀딩 수 */
            int totalParticipations,
            /** 진행중인 펀딩 수 */
            int active,
            /** 종료된 펀딩 수 (성공/실패 합) */
            int ended
            /** 발송준비중인 펀딩 수 (배송 CRUD 완성 후 활성화) */
            // int fulfilling,
            /** 발송완료된 펀딩 수 (배송 CRUD 완성 후 활성화) */
            // int fulfilled
    ) {
    }

    /**
     * 펀딩 참여 정보
     */
    public record Participation(
            /** 참여번호 (리스트 표기용 짧은 번호) */
            String participationNumber,
            /** 내부 식별자 (FundingContribution ID) */
            Long participationId,
            /** 이미지 URL */
            String imageUrl,
            /** 펀딩 제목 */
            String title,
            /** 작가 정보 */
            Artist artist,
            /** 수량 */
            int quantity,
            /** 후원 금액 */
            long pledgedAmount,
            /** 상태 (ACTIVE | ENDED) */
            String status,
            /** 상태 텍스트 */
            String statusText,
            /** 후원 일자 (YYYY-MM-DD) */
            String paidDate,
            /** 링크 정보 */
            Link links,
            /** 메타 정보 (목록 조회 시 null, 상세 조회 시 포함) */
            Meta meta
    ) {
    }

    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID */
            Long id,
            /** 작가명 */
            String name
    ) {
    }

    /**
     * 링크 정보
     */
    public record Link(
            /** 상세 페이지 URL */
            String detail
    ) {
    }

    /**
     * 메타 정보 (선택적)
     * 이거는 프론트와 상의 후 결정
     */
    public record Meta(
            /** 펀딩 ID */
            Long fundingId,
            /** 펀딩 번호 */
            String fundingNumber,
            /** 달성률 */
            int achievementRate,
            /** 현재 모금액 */
            long currentAmount,
            /** 목표 금액 */
            long targetAmount,
            /** 서포터 수 */
            int supporterCount,
            /** 사용자 후원 정보 */
            UserPledge userPledge,
            /** 권한 정보 */
            Permission permissions
    ) {
    }

    /**
     * 사용자 후원 정보
     */
    public record UserPledge(
            /** 리워드 티어명 */
            String rewardTierName,
            /** 후원 일시 */
            LocalDateTime pledgedAt,
            /** 원시 상태 (내부용) */
            String rawStatus
    ) {
    }

    /**
     * 권한 정보
     */
    public record Permission(
            /** 후원 취소 가능 여부 */
            Boolean canCancelPledge,
            /** 배송 정보 수정 가능 여부 */
            Boolean canEditShippingInfo
    ) {
    }
}
