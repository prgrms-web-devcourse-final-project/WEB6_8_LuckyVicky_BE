package com.back.domain.product.product.repository;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 신상품 (최근 14일)
    @Query("SELECT p FROM Product p " +
            "WHERE p.createDate BETWEEN :fromDate AND :toDate " +
            "AND p.displayStatus = com.back.domain.product.product.entity.DisplayStatus.DISPLAYING " +
            "ORDER BY p.createDate DESC")
    List<Product> findRecentProducts(@Param("fromDate") LocalDateTime fromDate,
                                     @Param("toDate") LocalDateTime toDate);

    // 할인중 상품
    @Query("SELECT p FROM Product p " +
            "WHERE p.discountRate > 0 " +
            "AND p.displayStatus = com.back.domain.product.product.entity.DisplayStatus.DISPLAYING " +
            "ORDER BY p.discountRate DESC")
    List<Product> findOnSaleProducts();

    // 품절 임박 상품 (재고 5개 이하)
    @Query("SELECT p FROM Product p " +
            "WHERE p.stock <= 5 " +
            "AND p.displayStatus = com.back.domain.product.product.entity.DisplayStatus.DISPLAYING " +
            "ORDER BY p.stock ASC")
    List<Product> findLowStockProducts();

    // 재입고 상품
    @Query("SELECT p FROM Product p " +
            "WHERE p.isRestock = true " +
            "AND p.displayStatus = com.back.domain.product.product.entity.DisplayStatus.DISPLAYING " +
            "ORDER BY p.createDate DESC")
    List<Product> findRestockProducts();

    // 기획 상품
    @Query("SELECT p FROM Product p " +
            "WHERE p.isPlanned = true " +
            "AND p.displayStatus = com.back.domain.product.product.entity.DisplayStatus.DISPLAYING " +
            "ORDER BY p.createDate DESC")
    List<Product> findPlannedProducts();

    // 오픈 예정 상품
    @Query("SELECT p FROM Product p " +
            "WHERE p.sellingStartDate > :today " +
            "AND p.displayStatus = com.back.domain.product.product.entity.DisplayStatus.DISPLAYING " +
            "ORDER BY p.sellingStartDate ASC")
    List<Product> findUpcomingProducts(@Param("today") LocalDateTime today);
}
