package com.back.domain.cart.dto.response;

import com.back.domain.cart.entity.Cart;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 장바구니 개별 상품 응답 DTO
 */
@Getter
@Builder
public class CartResponseDto {

    private Long cartId; // 장바구니 아이템 ID
    private Long productId; // 상품 ID
    private String productName; // 상품명
    private String productImageUrl; // 상품 이미지 URL
    private Integer price; // 상품 가격
    private Integer quantity; // 수량
    private String optionInfo; // 옵션 정보
    private Boolean isSelected; // 선택 여부 (체크마크)
    private String cartType; // 장바구니 타입
    private LocalDateTime createdAt; // 생성일시

    /**
     * Cart 엔티티를 CartResponseDto로 변환
     */
    public static CartResponseDto from(Cart cart) {
        return CartResponseDto.builder()
                .cartId(cart.getId())
                .productId(cart.getProduct().getId())
                .productName(cart.getProduct().getName())
                //.productImageUrl(cart.getProduct().getMainImageUrl()) // Product 엔티티에 있을 메서드
                //.price(cart.getProduct().getPrice().intValue())
                .quantity(cart.getQuantity())
                .optionInfo(cart.getOptionInfo())
                .isSelected(cart.getIsSelected())
                .cartType(cart.getCartType().name())
                .createdAt(cart.getCreateDate())
                .build();
    }
}
