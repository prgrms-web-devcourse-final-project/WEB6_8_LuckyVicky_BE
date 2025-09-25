package com.back.domain.dashboard.artist.dto.response;

import com.back.global.util.PageResponse;
import lombok.*;

import java.util.List;

/**
 * 작가 펀딩 관련 응답 DTO
 * <p>
 * 작가가 신청한 펀딩 목록과 관련된 정보를 포함합니다.
 * 2025.09.24 생성
 */
public class ArtistFundingResponse {

    /**
     * 펀딩 목록 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class List extends PageResponse<ArtistFundingResponse.Funding> {

        /**
         * 펀딩 요약 정보
         */
        private Summary summary;
        /**
         * 일괄 작업 목록
         */
        private java.util.List<BulkAction> bulkActions;

        public List(Summary summary, java.util.List<Funding> content, java.util.List<BulkAction> bulkActions,
                    int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
            this.bulkActions = bulkActions;
        }
    }

    /**
     * 펀딩 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /**
         * 전체 펀딩 수
         */
        private int totalFundings;
        /**
         * 진행중 펀딩 수
         */
        private int activeFundings;
        /**
         * 완료된 펀딩 수
         */
        private int completedFundings;
        /**
         * 취소된 펀딩 수
         */
        private int cancelledFundings;
    }

    /**
     * 펀딩 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Funding {
        /**
         * 펀딩 ID
         */
        private Long fundingId;
        /**
         * 펀딩 제목
         */
        private String title;
        /**
         * 펀딩 상태
         */
        private String status; // ACTIVE, COMPLETED, CANCELLED, PAUSED, PENDING
        /**
         * 목표 금액
         */
        private int targetAmount;
        /**
         * 현재 금액
         */
        private int currentAmount;
        /**
         * 달성률 (%)
         */
        private int achievementRate;
        /**
         * 후원자 수
         */
        private int supporterCount;
        /**
         * 시작일
         */
        private String startDate;
        /**
         * 종료일
         */
        private String endDate;
        /**
         * 등록일
         */
        private String registeredAt;
        /**
         * 메인 이미지 URL
         */
        private String mainImage;
        /**
         * 카테고리 정보
         */
        private Category category;
        /**
         * 권한 정보
         */
        private Permissions permissions;
        /**
         * 플래그 정보
         */
        private Flags flags;
    }

    /**
     * 카테고리 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        /**
         * 카테고리 ID
         */
        private Long id;
        /**
         * 카테고리 명
         */
        private String name;
    }

    /**
     * 권한 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permissions {
        /**
         * 편집 권한
         */
        private boolean canEdit;
        /**
         * 취소 권한
         */
        private boolean canCancel;
        /**
         * 판매 요청 권한
         */
        private boolean canRequestSale;
    }

    /**
     * 플래그 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Flags {
        /**
         * 목표 달성 여부
         */
        private boolean goalAchieved;
        /**
         * 마감 임박 여부
         */
        private boolean dueSoon;
        /**
         * 마감 여부
         */
        private boolean ended;
    }

    /**
     * 일괄 작업 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkAction {
        /**
         * 작업 액션
         */
        private String action;
        /**
         * 작업 라벨
         */
        private String label;
        /**
         * 확인 필요 여부
         */
        private boolean requiresConfirmation;
    }
}