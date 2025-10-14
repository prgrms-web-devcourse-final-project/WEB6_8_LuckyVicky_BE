package com.back.domain.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 리뷰 목록 조회 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewListRequestDto {

    private UUID productUuid; // 상품 UUID

    private ReviewType reviewType; // 리뷰 타입 (PHOTO, GENERAL, ALL)

    private Integer page; // 페이지 번호 (기본값: 0)

    private Integer size; // 페이지 크기 (기본값: 10)

    /**
     * 리뷰 타입 enum
     */
    public enum ReviewType {
        PHOTO,      // 포토리뷰
        GENERAL,    // 일반리뷰
        ALL         // 전체
    }
}
