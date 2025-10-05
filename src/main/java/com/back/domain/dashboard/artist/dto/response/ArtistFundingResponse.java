package com.back.domain.dashboard.artist.dto.response;

import com.back.global.util.PageResponse;

/**
 * 작가 펀딩 관련 응답 DTO
 * <p>
 * 작가가 신청한 펀딩 목록과 관련된 정보를 포함합니다.
 * 2025.09.30 수정 - 펀딩 CRUD와 일치하도록 필드 타입 및 명칭 변경
 */
public class ArtistFundingResponse {

    /**
     * 펀딩 목록 응답
     */
    public static class List extends PageResponse<ArtistFundingResponse.Funding> {

        public List() {
            super();
        }

        public List(java.util.List<Funding> content,
                    int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
        }
    }

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
             * 펀딩 상태 (OPEN, CLOSED, SUCCESS, FAILED, CANCELED)
             */
            String status,
            /**
             * 펀딩 상태 텍스트
             */
            String statusText,
            /**
             * 목표 금액
             */
            long targetAmount,
            /**
             * 현재 모금액
             */
            long currentAmount,
            /**
             * 달성률 (%)
             */
            double achievementRate,
            /**
             * 후원자 수
             */
            int participantCount,
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
             * 카테고리 정보 (현재 미사용)
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
             * 뉴스 작성 권한
             */
            boolean canCreateNews
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
             * 마감 임박 여부 (7일 이내)
             */
            boolean dueSoon,
            /**
             * 마감 여부
             */
            boolean ended
    ) {}
}
