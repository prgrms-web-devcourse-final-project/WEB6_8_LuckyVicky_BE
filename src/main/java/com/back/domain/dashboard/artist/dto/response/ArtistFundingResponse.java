package com.back.domain.dashboard.artist.dto.response;

import com.back.global.util.PageResponse;

/**
 * 작가 펀딩 관련 응답 DTO
 * <p>
 * 작가가 신청한 펀딩 목록과 관련된 정보를 포함합니다.
 * 2025.09.25 수정
 */
public class ArtistFundingResponse {

    /**
     * 펀딩 목록 응답
     */
    public static class List extends PageResponse<ArtistFundingResponse.Funding> {

        /**
         * 펀딩 요약 정보
         */
        private final Summary summary;
        /**
         * 일괄 작업 목록
         */
        private final java.util.List<BulkAction> bulkActions;

        public List(Summary summary, java.util.List<Funding> content, java.util.List<BulkAction> bulkActions,
                    int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
            this.bulkActions = bulkActions;
        }

        public Summary getSummary() {
            return summary;
        }

        public java.util.List<BulkAction> getBulkActions() {
            return bulkActions;
        }
    }

    /**
     * 펀딩 요약 정보
     */
    public record Summary(
            /**
             * 전체 펀딩 수
             */
            int totalFundings,
            /**
             * 진행중 펀딩 수
             */
            int activeFundings,
            /**
             * 완료된 펀딩 수
             */
            int completedFundings,
            /**
             * 취소된 펀딩 수
             */
            int cancelledFundings
    ) {}

    /**
     * 펀딩 정보
     */
    public record Funding(
            /**
             * 펀딩 ID
             */
            Long fundingId,
            /**
             * 펀딩 제목
             */
            String title,
            /**
             * 펀딩 상태
             */
            String status, // ACTIVE, COMPLETED, CANCELLED, PAUSED, PENDING
            /**
             * 목표 금액
             */
            int targetAmount,
            /**
             * 현재 금액
             */
            int currentAmount,
            /**
             * 달성률 (%)
             */
            int achievementRate,
            /**
             * 후원자 수
             */
            int supporterCount,
            /**
             * 시작일
             */
            String startDate,
            /**
             * 종료일
             */
            String endDate,
            /**
             * 등록일
             */
            String registeredAt,
            /**
             * 메인 이미지 URL
             */
            String mainImage,
            /**
             * 카테고리 정보
             */
            Category category,
            /**
             * 권한 정보
             */
            Permissions permissions,
            /**
             * 플래그 정보
             */
            Flags flags
    ) {}

    /**
     * 카테고리 정보
     */
    public record Category(
            /**
             * 카테고리 ID
             */
            Long id,
            /**
             * 카테고리 명
             */
            String name
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /**
             * 편집 권한
             */
            boolean canEdit,
            /**
             * 취소 권한
             */
            boolean canCancel,
            /**
             * 판매 요청 권한
             */
            boolean canRequestSale
    ) {}

    /**
     * 플래그 정보
     */
    public record Flags(
            /**
             * 목표 달성 여부
             */
            boolean goalAchieved,
            /**
             * 마감 임박 여부
             */
            boolean dueSoon,
            /**
             * 마감 여부
             */
            boolean ended
    ) {}

    /**
     * 일괄 작업 정보
     */
    public record BulkAction(
            /**
             * 작업 액션
             */
            String action,
            /**
             * 작업 라벨
             */
            String label,
            /**
             * 확인 필요 여부
             */
            boolean requiresConfirmation
    ) {}
}
