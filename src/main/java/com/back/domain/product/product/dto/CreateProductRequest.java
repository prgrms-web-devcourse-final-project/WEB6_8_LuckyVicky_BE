package com.back.domain.product.product.dto;

import com.back.global.s3.S3FileRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

// 상품 등록 요청 데이터를 받는 record
public record CreateProductRequest(
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId, // 카테고리 id

        @NotBlank(message = "상품명은 필수입니다.")
        String name, // 상품명

        @NotBlank(message = "브랜드명은 필수입니다.")
        String brandName, // 브랜드명

        @Min(value = 1, message = "정가는 최소 1 이상이어야 합니다.")
        int price,// 정가

        @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
        @Max(value = 100, message = "할인율은 100 이하이어야 합니다.")
        int discountRate,// 할인율

        @NotNull(message = "묶음 배송 여부는 필수입니다.")
        Boolean bundleShippingAvailable, // 묶음 배송 가능 여부

        @Min(value = 1, message = "기본 배송비는 최소 1 이상이어야 합니다.")
        int deliveryCharge,// 기본 배송비

        @Min(value = 1, message = "추가 배송비는 최소 1 이상이어야 합니다.")
        int additionalShippingCharges, // 추가 배송비

        @NotBlank(message = "배송비 유형은 필수입니다.")
        String deliveryType, // 배송비 유형

        Integer conditionalFreeAmount, // 조건부 무료 기준 금액

        @Min(value = 1, message = "재고는 최소 1 이상이어야 합니다.")
        int stock, // 재고

        @NotBlank(message = "상품 상세 설명은 필수입니다.")
        String description, // 상품 상세 설명

        @NotBlank(message = "판매 상태는 필수입니다.")
        String sellingStatus, // 판매 상태

        @NotBlank(message = "전시 상태는 필수입니다.")
        String displayStatus, // 전시 상태

        @Min(value = 1, message = "최소 구매 수량은 1 이상이어야 합니다.")
        int minQuantity, // 최소 구매 수량

        @Min(value = 1, message = "최대 구매 수량은 1 이상이어야 합니다.")
        int maxQuantity, // 최대 구매 수량

        @NotNull(message = "기획상품 여부는 필수입니다.")
        Boolean isPlanned, // 기획상품 여부

        @NotNull(message = "재입고 여부는 필수입니다.")
        Boolean isRestock,// 재입고 여부

        LocalDate sellingStartDate, // 판매 시작일

        LocalDate sellingEndDate, // 판매 종료일

        @NotEmpty(message = "스타일 태그는 최소 1개 이상이어야 합니다.")
        List<Long> tags, // 스타일 태그 id 목록

        @Valid
        List<Option> options, // 옵션 목록

        @Valid
        List<AdditionalProduct> additionalProducts, // 추가 상품 목록

        @NotEmpty(message = "이미지는 최소 1개 이상이어야 합니다.")
        @Valid
        List<S3FileRequest> images, // 업로드 완료된 이미지 정보

        @NotBlank(message = "품명/모델명은 필수입니다.")
        String productModelName, // 품명/모델명

        @NotNull(message = "KC 인증 여부는 필수입니다.")
        Boolean certification,// KC 인증 여부

        @NotBlank(message = "제조국은 필수입니다.")
        String origin, // 제조국

        @NotBlank(message = "재질은 필수입니다.")
        String material, // 재질

        @NotBlank(message = "사이즈는 필수입니다.")
        String size // 사이즈
) {

    public record Option(
            @NotBlank(message = "옵션명은 필수입니다.")
            String optionName, // 옵션명

            @Min(value = 1, message = "옵션 재고는 1 이상이어야 합니다.")
            int optionStock, // 옵션 재고

            @Min(value = 0, message = "옵션 추가금은 0 이상이어야 합니다.")
            int optionAdditionalPrice // 옵션 추가금
    ) {}

    public record AdditionalProduct(
            @NotBlank(message = "추가상품명은 필수입니다.")
            String additionalProductName,

            @Min(value = 1, message = "추가상품 재고는 1 이상이어야 합니다.")
            int additionalProductStock,

            @Min(value = 0, message = "추가상품 가격은 0 이상이어야 합니다.")
            int additionalProductPrice
    ) {}
}

