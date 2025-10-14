package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

/**
 * 팔로우 관련 응답 DTO
 * <p>
 * 사용자가 팔로우한 작가들의 정보를 포함
 * 2025.09.22 수정 - API 명세 변경에 따른 구조 개편
 * 2025.10.14 수정 - Figma 디자인에 맞춰 불필요한 필드 제거
 */
public class FollowingResponse {

    /**
     * 팔로우한 작가 목록 응답
     */
    public static class List extends PageResponse<FollowingResponse.Artist> {

        public List() {
            super();
        }

        public List(java.util.List<Artist> content,
                    int page, int size, long totalElements, int totalPages,
                    boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
        }
    }

    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID */
            String artistId,
            /** 작가명 */
            String artistName,
            /** 프로필 이미지 URL (null인 경우 프론트에서 기본 이미지 표시) */
            String profileImageUrl,
            /** 작가 페이지 URL */
            String artistPageUrl
    ) {
    }
}
