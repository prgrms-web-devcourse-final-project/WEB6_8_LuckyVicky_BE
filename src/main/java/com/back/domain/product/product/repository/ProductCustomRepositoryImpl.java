package com.back.domain.product.product.repository;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.dto.response.ProductListResponse.ProductInfo;
import com.back.domain.product.product.entity.*;
import com.back.global.s3.FileType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductCustomRepository 구현체
 * - QueryDSL 사용으로 동적 조건 처리
 * - THUMBNAIL 이미지 join, 태그/카테고리/가격/배송 필터, 정렬, 페이징 처리
 * - 하위 카테고리 포함 조회 기능 추가
 */
@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductListResponse findProducts(
            Long categoryId, // 카테고리 id-> 이걸로 상위 또는 하위 카테고리 조회 가능 (필터링)
            List<Long> tagIds, // 태그 id(필터링)
            Integer minPrice,//최소가격 설정 (필터링)
            Integer maxPrice,//최대가격 설정 (필터링)
            String deliveryType, // 배송비 유형 (필터링)
            String sort, // (정렬) 신상품순, 가격낮은순, 가격높은순, 인기순
            Pageable pageable // (페이징)
    ) {
        // Q클래스
        QProduct p = QProduct.product;
        QProductImage img = QProductImage.productImage;

        // 동적 조건을 누적해서 where절에 적용
        BooleanBuilder builder = new BooleanBuilder();

        // 전시중인 상품만 조회
        builder.and(p.displayStatus.eq(DisplayStatus.DISPLAYING));

        // 카테고리 필터
        if (categoryId != null) {
            if (isTopCategory(categoryId)) { // true면 상위 카테고리
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(categoryId); // 상위 카테고리 id 포함
                categoryIds.addAll(getSubCategoryIds(categoryId)); // 해당 하위 카테고리 ID까지 수집
                builder.and(p.category.id.in(categoryIds)); // 상위+하위 카테고리 상품 조회
            } else { // false면 하위 카테고리
                builder.and(p.category.id.eq(categoryId)); // 하위 카테고리만 조회
            }
        }

        // 태그 필터
        if (tagIds != null && !tagIds.isEmpty()) {
            builder.and(p.productTags.any().tag.id.in(tagIds)); // 상품에 사용자가 필터링으로 선택한 tagIds가 하나라도 있으면 조건 만족
        }

        // 할인된 최종 가격에 대한 표현식 생성
        com.querydsl.core.types.dsl.NumberExpression<Integer> discountedPrice = p.price.subtract(p.price.multiply(p.discountRate).divide(100));

        // 가격대 필터 (할인된 가격 기준)
        if (minPrice != null) builder.and(discountedPrice.goe(minPrice));
        if (maxPrice != null) builder.and(discountedPrice.loe(maxPrice));

        // 배송유형 필터
        if (deliveryType != null) {
            builder.and(p.deliveryType.eq(
                    com.back.domain.product.product.entity.DeliveryType.valueOf(deliveryType)
            ));
        }

        // QueryDSL로 엔티티 조회 + THUMBNAIL join
        var query = queryFactory
                .select(p)
                .from(p)
                .leftJoin(p.images, img).on(img.fileType.eq(FileType.THUMBNAIL))
                .where(builder)
                .distinct();

        // 정렬 처리
        if ("priceAsc".equals(sort)) query.orderBy(p.price.asc()); // 가격 낮은 순
        else if ("priceDesc".equals(sort)) query.orderBy(p.price.desc()); // 가격 높은 순
        else query.orderBy(p.createDate.desc()); // 일단 기본은 신상품순으로 함.

        // 전체 건수 조회 (페이징용)
        long total = query.fetchCount();

        // 페이징 적용하여 엔티티 조회
        List<Product> fetchedProducts = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 엔티티를 DTO로 변환
        List<ProductInfo> products = fetchedProducts.stream()
                .map(product -> {
                    String thumbnailUrl = product.getImages().stream()
                            .filter(i -> i.getFileType() == FileType.THUMBNAIL)
                            .findFirst()
                            .map(ProductImage::getFileUrl)
                            .orElse(null);

                    return new ProductInfo(
                            product.getProductUuid(),
                            thumbnailUrl,
                            product.getBrandName(),
                            product.getName(),
                            product.getPrice(),
                            product.getDiscountRate(),
                            product.getDiscountPrice(),
                            product.getAverageRating()
                    );
                })
                .toList();


        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());

        // 최종 DTO 반환
        return new ProductListResponse(
                pageable.getPageNumber(), // 현재 페이지 번호
                pageable.getPageSize(), // 한 페이지에 보여줄 상품 수
                total, // 전체 상품 수
                totalPages, // 전체 페이지 수
                products // 상품 조회 결과 리스트
        );
    }

    /**
     * 상위 카테고리 여부 확인
     * parentId가 null이면 상위 카테고리이고, null이 아니면 하위 카테고리
     */
    private boolean isTopCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(c -> c.getParent() == null) // 상위 카테고리: parentId == null
                .orElse(false); // 없으면 false
    }

    /**
     * 상위 카테고리의 바로 아래 하위 카테고리 ID 조회
     */
    private List<Long> getSubCategoryIds(Long categoryId) {
        // parentId가 categoryId인 하위 카테고리 ID 리스트 반환
        return categoryRepository.findAllByParentId(categoryId)
                .stream()
                .map(Category::getId)
                .toList();
    }

    /**
     * 작가의 상품 목록 조회 (대시보드용)
     */
    @Override
    public org.springframework.data.domain.Page<com.back.domain.product.product.entity.Product> findProductsByArtist(
            Long userId,
            String keyword,
            Boolean selling,
            String sort,
            String order,
            org.springframework.data.domain.Pageable pageable
    ) {
        QProduct p = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        // 작가 ID 필터 (필수)
        builder.and(p.user.id.eq(userId));

        // 논리 삭제된 상품 제외
        builder.and(p.isDeleted.eq(false));

        // 검색 키워드 필터
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(p.name.containsIgnoreCase(keyword));
        }

        // 판매 중 필터
        if (Boolean.TRUE.equals(selling)) {
            builder.and(p.sellingStatus.eq(
                    com.back.domain.product.product.entity.SellingStatus.SELLING
            ));
        }

        // 기본 쿼리
        var query = queryFactory
                .selectFrom(p)
                .where(builder);

        // 정렬 처리
        if ("name".equals(sort)) {
            query.orderBy("ASC".equals(order) ? p.name.asc() : p.name.desc());
        } else if ("price".equals(sort)) {
            query.orderBy("ASC".equals(order) ? p.price.asc() : p.price.desc());
        } else if ("sellingStatus".equals(sort)) {
            query.orderBy("ASC".equals(order) ? p.sellingStatus.asc() : p.sellingStatus.desc());
        } else { // 기본: createDate
            query.orderBy("ASC".equals(order) ? p.createDate.asc() : p.createDate.desc());
        }

        // 전체 건수
        long total = query.fetchCount();

        // 페이징 적용
        java.util.List<com.back.domain.product.product.entity.Product> products = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Page 객체로 반환
        return new org.springframework.data.domain.PageImpl<>(products, pageable, total);
    }

    /**
     * 검색 키워드(상품명)에 해당하는 상품 조회
     */
    @Override
    public List<Product> searchByProductNameOrBrandName(String keyword) {
        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        if (org.springframework.util.StringUtils.hasText(keyword)) {
            String[] keywords = keyword.trim().toLowerCase().split("\\s+");
            for (String kw : keywords) {
                builder.or(product.name.toLowerCase().contains(kw)
                        .or(product.brandName.toLowerCase().contains(kw))); // 상품명 또는 브랜드명에서 찾기
            }
        }

        // 전시중인 상품만 조회
        builder.and(product.displayStatus.eq(DisplayStatus.DISPLAYING));

        return queryFactory
                .selectFrom(product)
                .where(builder)
                .distinct()
                .fetch();
    }
}
