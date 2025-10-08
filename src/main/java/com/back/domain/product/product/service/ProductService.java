package com.back.domain.product.product.service;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.request.CreateProductRequest;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.dto.response.ShareLinkResponse;
import com.back.domain.product.product.entity.*;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.product.tag.entity.Tag;
import com.back.domain.product.tag.repository.TagRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.FileType;
import com.back.global.s3.S3ValidationService;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository TagRepository;
    private final S3ValidationService s3ValidationService;
    private final UserRepository userRepository;

    @Value("${app.frontend-url:https://mori-mori.store}")
    private String frontendUrl;

    // 상품 등록
    @Transactional
    public UUID createProduct(CreateProductRequest request, CustomUserDetails customUserDetails) {

        // 현재 로그인한 사용자 (=상품 등록한 작가)
        User user = customUserDetails.getUser();
        // 권한 체크(ARTIST,ADMIN,ROOT만 상품 등록 가능)
        if (!Set.of(Role.ARTIST, Role.ADMIN, Role.ROOT).contains(user.getRole())) {
            throw new ServiceException("403", "상품 등록 권한이 없습니다.");
        }

        // 존재하는 카테고리인지 검증
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ServiceException("400", "존재하지 않는 카테고리입니다."));

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
                .sellingStatus(request.sellingStatus() != null ? SellingStatus.valueOf(request.sellingStatus()) : SellingStatus.SELLING) //판매상태(null이면 기본값 판매중으로)
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
                                .orElseThrow(() -> new ServiceException("400", "존재하지 않는 태그입니다. tagId: " + tagId));
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
                    .map(imgRequest -> {
                        s3ValidationService.validateFileExists(imgRequest.s3Key());
                        return ProductImage.builder()
                                .product(product)
                                .fileUrl(imgRequest.url())
                                .fileType(imgRequest.type())
                                .s3Key(imgRequest.s3Key())
                                .originalFilename(imgRequest.originalFileName())
                                .build();
                    })
                    .collect(Collectors.toList());
            product.getImages().addAll(images);
        }

        // cascade로 옵션, 추가상품, 이미지, 태그까지 같이 저장됨
        productRepository.save(product);

        // 등록된 상품의 pk 반환
        return product.getProductUuid();
    }


    // (상품) 파일 다운로드 메서드
    @Transactional(readOnly = true)
    public ProductImage getProductDocument(UUID productUuid) {
        // productUuid로 상품 조회
        Product product = productRepository.findByProductUuid(productUuid)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 상품입니다. productUuid: " + productUuid));
        return product.getImages().stream()
                .filter(img -> img.getFileType() == FileType.DOCUMENT)
                .findFirst()
                .orElseThrow(() -> new ServiceException("404", "다운로드할 문서가 존재하지 않습니다."));
    }

    // 상품 목록 조회
    @Transactional(readOnly = true)
    public ProductListResponse getProducts(
            Long categoryId,
            List<Long> tagIds,
            Integer minPrice,
            Integer maxPrice,
            String deliveryType,
            String sort,
            Pageable pageable
    ) {
        // ProductCustomRepositoryImpl의 findProducts 호출 -> QueryDSL로 동적 쿼리 생성(필터링/정렬)+페이징+DTO반환
        return productRepository.findProducts(
                categoryId,
                tagIds,
                minPrice,
                maxPrice,
                deliveryType,
                sort,
                pageable
        );
    }

    /**
     * 상품 공유 링크 생성 (UTM 파라미터 포함)
     * 
     * 누구나 상품을 공유할 수 있으며, 작가 ID는 상품 소유자 기준으로 설정됨
     * UTM 파라미터를 통해 작가별 유입 경로 추적 가능
     * 
     * @param productUuid 상품 UUID
     * @param platform 공유할 플랫폼 (instagram, youtube, naver 등)
     * @param customUserDetails 현재 로그인한 사용자 정보 (선택사항)
     * @return UTM 파라미터가 포함된 공유 링크
     */
    @Transactional(readOnly = true)
    public ShareLinkResponse generateShareLink(UUID productUuid, String platform, CustomUserDetails customUserDetails) {
        log.info("공유 링크 생성 시작 - 상품 UUID: {}, 플랫폼: {}", productUuid, platform);

        // 상품 존재 여부 확인
        Product product = productRepository.findByProductUuid(productUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. UUID: " + productUuid));

        // 논리 삭제된 상품은 공유 불가
        if (product.isDeleted()) {
            throw new IllegalArgumentException("삭제된 상품은 공유할 수 없습니다.");
        }

        // 판매 중이 아닌 상품은 공유 불가 (선택적)
        if (product.getSellingStatus() != SellingStatus.SELLING) {
            log.warn("판매 중이 아닌 상품 공유 시도 - productUuid: {}, status: {}", 
                     productUuid, product.getSellingStatus());
            // 경고만 하고 공유는 허용 (작가가 예약 판매 등을 미리 공유할 수 있도록)
        }

        // 상품 소유자(작가)의 ID 사용
        Long artistId = product.getUser().getId();

        // 플랫폼 검증 및 정규화
        String normalizedPlatform = validateAndNormalizePlatform(platform);

        // 베이스 URL 생성 (프론트엔드 URL)
        String baseUrl = frontendUrl + "/product/" + productUuid;

        // UTM 파라미터 생성
        // utm_source: 유입 경로 (instagram, youtube 등)
        // utm_medium: 매체 타입 (social 고정)
        // utm_campaign: 캠페인 (작가 ID 포함)
        // utm_content: 추가 정보 (product_share 고정)
        String utmParams = String.format(
                "?utm_source=%s&utm_medium=social&utm_campaign=artist_%d&utm_content=product_share",
                normalizedPlatform,
                artistId
        );

        String shareLink = baseUrl + utmParams;

        log.info("공유 링크 생성 완료 - 작가 ID: {}, 상품 UUID: {}, 플랫폼: {}", 
                 artistId, productUuid, normalizedPlatform);

        return new ShareLinkResponse(
                shareLink,
                normalizedPlatform,
                artistId,
                productUuid,
                product.getName() // 상품명을 설명으로 사용
        );
    }

    /**
     * 플랫폼 검증 및 정규화
     * 
     * @param platform 플랫폼명
     * @return 정규화된 플랫폼명 (소문자)
     */
    private String validateAndNormalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("플랫폼을 지정해야 합니다.");
        }

        // 소문자로 변환
        String normalized = platform.toLowerCase().trim();

        // 지원하는 플랫폼 목록
        Set<String> supportedPlatforms = Set.of(
                "instagram", "youtube", "naver", "kakao", "facebook", 
                "twitter", "band", "pinterest", "tiktok", "linkedin"
        );

        if (!supportedPlatforms.contains(normalized)) {
            log.warn("지원하지 않는 플랫폼이지만 허용: {}", normalized);
            // 지원하지 않는 플랫폼도 일단 허용 (GA4에서 추적은 됨)
        }

        return normalized;
    }

}
