package com.back.domain.product.product.repository;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.dto.response.ProductListResponse.ProductInfo;
import com.back.domain.product.product.entity.QProduct;
import com.back.domain.product.product.entity.QProductImage;
import com.back.global.s3.FileType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
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

        // 가격대 필터
        if (minPrice != null) builder.and(p.price.goe(minPrice)); // 최소 가격 이상 (goe는 greater or equal)
        if (maxPrice != null) builder.and(p.price.loe(maxPrice)); // 최대 가격 이하 (loe는 less or equal)

        // 배송유형 필터
        if (deliveryType != null) {
            builder.and(p.deliveryType.eq(
                    com.back.domain.product.product.entity.DeliveryType.valueOf(deliveryType)
            ));
        }

        // QueryDSL로 DTO 매핑 + THUMBNAIL join
        var query = queryFactory
                .select(Projections.constructor(
                        ProductInfo.class, // dto 매핑
                        p.id, // 상품 id
                        img.fileUrl, //썸네일 이미지 url
                        p.brandName, // 브랜드명
                        p.name, // 상품명
                        p.price, // 가격
                        p.discountRate,// 할인율
                        p.price.subtract(p.price.multiply(p.discountRate).divide(100)), // 할인된 최종 가격
                        null // rating: 리뷰 연동 전이라서 일단 null로 함.
                ))
                .from(p)
                .leftJoin(p.images, img) // left join
                .on(img.fileType.eq(FileType.THUMBNAIL)) // type이 THUMBNAIL인 이미지만 JOIN
                .where(builder) // 동적 조건 적용
                .distinct(); // 중복 제거

        // 정렬 처리
        if ("priceAsc".equals(sort)) query.orderBy(p.price.asc()); // 가격 낮은 순
        else if ("priceDesc".equals(sort)) query.orderBy(p.price.desc()); // 가격 높은 순
        else query.orderBy(p.createDate.desc()); // 일단 기본은 신상품순으로 함.

        // 전체 건수 조회 (페이징용)
        long total = query.fetchCount();

        // 페이징 적용
        List<ProductInfo> products = query
                .offset(pageable.getOffset()) // offset
                .limit(pageable.getPageSize()) //limit
                .fetch();

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
}
