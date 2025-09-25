package com.back.domain.product.product.entity;

import com.back.domain.product.category.entity.Category;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 카테고리 FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작가 FK

    @Column(nullable = false)
    private String name; // 상품명

    @Column(nullable = false)
    private String brandName; // 브랜드명

    @Column(nullable = false)
    private int price; // 가격

    @Column(nullable = false)
    private int discountRate; // 할인율

    @Column(nullable = false)
    private boolean bundleShippingAvailable; // 묶음배송 가능여부

    @Column(nullable = false)
    private int deliveryCharge; // 기본 배송비

    @Column(nullable = false)
    private int additionalShippingCharge; // 제주 추가 배송비

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType; // 배송비 유형(무료,조건부무료,유료)

    private Integer conditionalFreeAmount; // 배송비 유형이 조건부 무료일 경우, 무료 배송 기준 금액

    @Column(nullable = false)
    private int stock; // 재고

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // 상품 정보 (텍스트+이미지 태그)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SellingStatus sellingStatus; // 판매 상태(판매전, 판매중, 품절, 판매종료)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DisplayStatus displayStatus; // 전시 상태(전시 전, 전시 중, 전시 종료)

    @Column(nullable = false)
    private int minQuantity; //최소 구매 수량

    @Column(nullable = false)
    private int maxQuantity; // 최대 구매 수량

    @Column(nullable = false)
    private String productModelName; // 품명(모델명)

    @Column(nullable = false)
    private boolean certification; // 법에 의한 인증,허가 확인사항

    @Column(nullable = false)
    private String origin; // 제조국

    @Column(nullable = false)
    private String material; // 재질

    @Column(nullable = false)
    private String size; // 사이즈

    @Column(nullable = false)
    private boolean isPlanned; // 기획 상품 여부

    @Column(nullable = false)
    private boolean isRestock; // 재입고 상품 여부

    private LocalDateTime sellingStartDate; // 판매 시작일

    private LocalDateTime sellingEndDate; // 판매 종료일

    @Column(nullable = false)
    private boolean isDeleted; // 논리 삭제 여부 (상품 삭제 시, 진짜 DB에서 삭제하냐 아니면 삭제 처리만 하냐 차이)


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true) // 상품이 저장/삭제되면 모든 옵션도 저장/삭제됨, 상품에서 옵션을 제거하면 DB에서 해당 옵션도 삭제됨.
    private List<Option> options; // 해당 상품의 옵션들

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true) // 상품이 저장/삭제되면 모든 추가상품도 저장/삭제됨, 상품에서 추가상품을 제거하면 DB에서 해당 추가상품도 삭제됨.
    private List<AdditionalProduct> additionalProducts; // 해당 상품의 추가상품들

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)// 상품이 저장/삭제되면 모든 상품이미지도 저장/삭제됨, 상품에서 이미지를 제거하면 DB에서 해당 이미지도 삭제됨.
    private List<ProductImage> images; // 해당 상품의 이미지들(대표/추가/썸네일 이미지)

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)// 상품이 저장/삭제되면 매핑 중간테이블의 해당 데이터도 저장/삭제됨, 상품에서 특정 태그를 제거하면 DB에서 해당 매핑 데이터도 삭제됨.
    private Set<ProductTagMapping> productTags; // 상품과 태그(스타일)의 중간 테이블. 하나의 상품에 동일한 태그를 중복으로 붙이는 걸 허용하지 않으므로 List말고 Set 사용

    // 할인된 가격 계산
    public int getDiscountPrice() {
        return price - (price * discountRate / 100);
    }
}
