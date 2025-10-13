package com.back.domain.dashboard.admin.dto.response;

/**
 * 관리자 펀딩 승인 대기 상세 조회 응답 DTO
 * 펀딩 상세보기 모달에서 사용
 * 2025.10.13 신규 생성
 */
public record AdminFundingApprovalDetailResponse(
        /** 펀딩 제목 */
        String fundingTitle,
        /** 작가 정보 */
        ArtistInfo artist,
        /** 사업자 정보 */
        BusinessInfo business
) {

    /**
     * 작가 정보
     */
    public record ArtistInfo(
            /** 작가 ID */
            Long id,
            /** 작가명 */
            String name,
            /** 이메일 */
            String email,
            /** 전화번호 */
            String phone
    ) {}

    /**
     * 사업자 정보
     */
    public record BusinessInfo(
            /** 사업자등록번호 */
            String businessNumber,
            /** 통신판매업 신고번호 */
            String telecomSalesNumber,
            /** 상호명 */
            String businessName,
            /** 사업장 주소 */
            String businessAddress
    ) {}
}
