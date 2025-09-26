package com.back.domain.cart.dto.response;

import com.back.domain.cart.entity.Cart;

import java.time.LocalDateTime;

/**
 * 장바구니 개별 상품 응답 DTO
 */
public record CartResponseDto(
    Long cartId, // 장바구니 아이템 ID
    Long productId, // 상품 ID
    String productName, // 상품명
    String productImageUrl, // 상품 이미지 URL
    Integer price, // 상품 가격
    Integer quantity, // 수량
    String optionInfo, // 옵션 정보
    Boolean isSelected, // 선택 여부 (체크마크)
    String cartType, // 장바구니 타입
    LocalDateTime createdAt // 생성일시
) {
    /**
     * Cart 엔티티를 CartResponseDto로 변환
     */
    public static CartResponseDto from(Cart cart) {
        Cart.ProductInfo productInfo = cart.getProductInfo();
        
        return new CartResponseDto(
                cart.getId(),
                productInfo.getId(),
                productInfo.getName(),
                productInfo.getImageUrl(), // ProductInfo에서 이미지 URL 가져오기
                productInfo.getPrice(),
                cart.getQuantity(),
                cart.getOptionInfo(),
                cart.isSelected(),
                cart.getCartType().name(),
                cart.getCreateDate()
        );
    }
}
