package com.back.domain.artist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 작가 사업자 정보 DTO
 */
public record ArtistBusinessInfoResponse(
        @Schema(description = "제조자", example = "(주) 문구문구")
        String businessName,
        @Schema(description = "사업자 등록 번호", example = "123-45-67890")
        String businessNumber,
        @Schema(description = "대표자명", example = "김작가")
        String ownerName,
        @Schema(description = "A/S 책임자 / 전화번호", example = "(주)문구문구/010-1234-5678")
        String asManager,
        @Schema(description = "전자우편주소", example = "example@company.com")
        String email,
        @Schema(description = "사업장 소재지", example = "서울특별시 강남구 테헤란로 1길 100 201호")
        String businessAddress,
        @Schema(description = "통신 판매업 신고 번호", example = "2023-서울강남-0001")
        String telecomSalesNumber
) {
}
