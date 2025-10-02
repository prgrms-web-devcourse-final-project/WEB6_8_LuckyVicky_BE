package com.back.domain.product.product.dto.response;

/**
 * 상품 공유 링크 응답 DTO
 * 
 * UTM 파라미터가 포함된 공유 링크를 제공
 * 2025.10.01 생성
 */
public record ShareLinkResponse(
        /** 공유 링크 (UTM 파라미터 포함) */
        String shareLink,
        
        /** 플랫폼 (instagram, youtube, naver 등) */
        String platform,
        
        /** 작가 ID */
        Long artistId,
        
        /** 상품 ID */
        Long productId,
        
        /** 짧은 설명 (선택) */
        String description
) {
    /**
     * 기본 생성자 (설명 없이)
     */
    public ShareLinkResponse(String shareLink, String platform, Long artistId, Long productId) {
        this(shareLink, platform, artistId, productId, null);
    }
}
