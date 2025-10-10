package com.back.domain.product.product.repository;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository, JpaSpecificationExecutor<Product> {
    Optional<Product> findByProductUuid(UUID productUuid);
    boolean existsByCategoryId(Long categoryId); // category_id 필드값이 해당 categoryId인 상품이 하나라도 존재하는지 체크
    
    /**
     * 특정 카테고리의 상품 수 조회
     */
    long countByCategoryAndIsDeletedFalse(Category category);

    /**
     * 작가의 상품 목록 조회 (삭제되지 않은 상품만)
     */
    List<Product> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    // 판매 시작일이 오늘 이후인 상품 조회
    List<Product> findBySellingStartDateAfter(LocalDateTime dateTime);

    // 판매 종료일이 오늘 이전인 상품 조회
    List<Product> findBySellingEndDateBefore(LocalDateTime dateTime);
}
