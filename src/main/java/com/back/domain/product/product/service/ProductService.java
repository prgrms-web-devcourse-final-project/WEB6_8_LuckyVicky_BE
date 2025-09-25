package com.back.domain.product.product.service;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.CreateProductRequest;
import com.back.domain.product.product.entity.*;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.product.tag.entity.Tag;
import com.back.domain.product.tag.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.global.s3.S3ValidationService;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository TagRepository;
    private final S3ValidationService s3ValidationService;

    // 상품 등록
    @Transactional
    public Long createProduct(CreateProductRequest request, CustomUserDetails customUserDetails) {

        // 존재하는 카테고리인지 검증
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 현재 로그인한 사용자 (=상품 등록한 작가)
        User user = customUserDetails.getUser();

        // Product 생성
        Product product = Product.builder()
                .category(category) // DTO에 있던 검증된 카테고리
                .user(user) // 작가 정보
                .name(request.name()) // 상품명
                .brandName(request.brandName()) // 브랜드명
                .price(request.price()) // 가격
                .discountRate(request.discountRate()) // 할인율
                .bundleShippingAvailable(request.bundleShippingAvailable()) // 묶음 배송 가능 여부
                .deliveryCharge(request.deliveryCharge()) // 기본 배송비
                .additionalShippingCharge(request.additionalShippingCharges()) // 제주 추가 배송비
                .deliveryType(DeliveryType.valueOf(request.deliveryType())) // 배송비 유형
                .conditionalFreeAmount(request.conditionalFreeAmount()) //조건부 배송일 경우 무료배송 조건 금액
                .stock(request.stock()) //재고
                .description(request.description()) // 상품 정보(텍스트+이미지 태그로 이루어진 HTML?)
                .sellingStatus(SellingStatus.valueOf(request.sellingStatus())) //판매상태
                .displayStatus(DisplayStatus.valueOf(request.displayStatus())) // 전시상태
                .minQuantity(request.minQuantity()) // 최소 구매수량
                .maxQuantity(request.maxQuantity()) // 최대 구매수량
                .isPlanned(request.isPlanned()) // 기획상품 여부
                .isRestock(request.isRestock()) // 재입고상품 여부
                .sellingStartDate(request.sellingStartDate() != null ? request.sellingStartDate().atStartOfDay() : null)// 판매시작일
                .sellingEndDate(request.sellingEndDate() != null ? request.sellingEndDate().atStartOfDay() : null)// 판매종료일
                .productModelName(request.productModelName()) //품명
                .certification(request.certification()) // 법에 의한 인증,허가 확인사항
                .origin(request.origin()) // 제조국
                .material(request.material()) // 재질
                .size(request.size()) //사이즈
                .isDeleted(false) // 논리삭제 여부(db에서 삭제는 안하고, 삭제처리만 된 것)
                .build();


        // 옵션이 존재한다면 Option 생성
        if (request.options() != null && !request.options().isEmpty()) {
            List<Option> options = request.options().stream()
                    .map(o -> Option.builder()
                            .product(product)
                            .optionName(o.optionName())
                            .optionStock(o.optionStock())
                            .optionAdditionalPrice(o.optionAdditionalPrice())
                            .build())
                    .collect(Collectors.toList());
            product.getOptions().addAll(options);
        }

        // 추가상품이 존재한다면 AdditionalProduct 생성
        if (request.additionalProducts() != null && !request.additionalProducts().isEmpty()) {
            List<AdditionalProduct> additionalProducts = request.additionalProducts().stream()
                    .map(a -> AdditionalProduct.builder()
                            .product(product)
                            .additionalName(a.additionalProductName())
                            .additionalStock(a.additionalProductStock())
                            .additionalPrice(a.additionalProductPrice())
                            .build())
                    .collect(Collectors.toList());
            product.getAdditionalProducts().addAll(additionalProducts);
        }

        // 태그 저장 (존재하는 태그인지 검증)
        if (request.tags() != null && !request.tags().isEmpty()) {
            List<ProductTagMapping> tagMappings = request.tags().stream()
                    .map(tagId -> {
                        Tag tag = TagRepository.findById(tagId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다. ID: " + tagId));
                        // 상품-태그 중간테이블 생성
                        return ProductTagMapping.builder()
                                .product(product)
                                .tag(tag)
                                .build();
                    })
                    .toList();
            product.getProductTags().addAll(tagMappings);
        }

        // 이미지 저장
        if (request.images() != null && !request.images().isEmpty()) {
            List<ProductImage> images = request.images().stream()
                    .peek(img -> s3ValidationService.validateFileExists(img.key())) // S3에 파일 존재 여부 검증
                    .map(img -> ProductImage.builder()
                            .product(product)
                            .fileUrl(img.url())
                            .fileType(img.type())
                            .key(img.key() != null ? img.key() : "")
                            .originalFilename(img.originalFileName() != null ? img.originalFileName() : "")
                            .build())
                    .collect(Collectors.toList());
            product.getImages().addAll(images);
        }

        // cascade로 옵션, 추가상품, 이미지, 태그까지 같이 저장됨
        productRepository.save(product);

        // 등록된 상품의 pk 반환
        return product.getId();
    }
}
