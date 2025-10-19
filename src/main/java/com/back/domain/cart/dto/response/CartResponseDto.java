package com.back.domain.cart.dto.response;

import com.back.domain.cart.entity.Cart;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 장바구니 개별 상품 응답 DTO
 */
public record CartResponseDto(
    Long cartId, // 장바구니 아이템 ID
    UUID productUuid, // 상품 UUID
    String productName, // 상품명
    String productImageUrl, // 상품 이미지 URL
    Integer price, // 상품 가격
    Integer quantity, // 수량
    String optionInfo, // 옵션 정보 (일반 상품용)
    Boolean isSelected, // 선택 여부 (체크마크)
    String cartType, // 장바구니 타입 (NORMAL, FUNDING)
    
    // 펀딩 전용 필드
    String fundingId, // 펀딩 고유 ID
    Integer fundingPrice, // 펀딩 단일 가격
    Integer fundingStock, // 펀딩 단일 재고
    
    LocalDateTime createdAt // 생성일시
) {
    /**
     * Cart 엔티티를 CartResponseDto로 변환
     */
    public static CartResponseDto from(Cart cart) {
        Cart.ProductInfo productInfo = cart.getProductInfo();
        
        return new CartResponseDto(
                cart.getId(),
                productInfo.getUuid(),
                productInfo.getName(),
                productInfo.getImageUrl(), // ProductInfo에서 이미지 URL 가져오기
                productInfo.getPrice(),
                cart.getQuantity(),
                cart.getOptionInfo(),
                cart.isSelected(),
                cart.getCartType().name(),
                
                // 펀딩 전용 필드
                cart.getFundingId(),
                cart.getFundingPrice(),
                cart.getFundingStock(),
                
                cart.getCreateDate()
        );
    }
}
