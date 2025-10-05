package com.back.domain.order.order.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 사용자별 주문 목록 (페이징)
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);

    // 주문 상세 조회 - Fetch Join (상품 정보만)
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);

    // 주문번호로 조회
    Optional<Order> findByOrderNumber(String orderNumber);

    // 상태별 조회 (페이징)
    Page<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status, Pageable pageable);

    // 단순 목록 조회
    List<Order> findByUserOrderByOrderDateDesc(User user);

    /**
     * 대시보드용 주문 목록 조회 (검색 + 정렬 + 페이징)
     * 상품명으로 검색
     * 4가지 배송 상태만 조회 (결제완료, 배송준비중, 배송중, 배송완료)
     * 취소/교환/환불 상태는 제외
     * 동적 정렬 지원 (날짜, 금액, 상태, 상품명)
     * 주문 ID만 조회 (페이징)
     * ID로 실제 데이터 FETCH JOIN - 기존 UUID가 24자리로 너무 길어서, 7자리로 포맷팅 이점 유의.
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.user = :user " +
            "AND o.status IN (com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED, " +
            "                 com.back.domain.order.order.entity.OrderStatus.PREPARING_SHIPMENT, " +
            "                 com.back.domain.order.order.entity.OrderStatus.SHIPPING, " +
            "                 com.back.domain.order.order.entity.OrderStatus.DELIVERED) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND oi.product.name LIKE CONCAT('%', :keyword, '%')))")
    Page<Order> findOrdersForDashboard(
            @Param("user") User user,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 대시보드용 주문 목록 조회 - 상품명 정렬용
     * 상품명 정렬 시 각 주문의 상품 중 이름이 가장 빠른 상품(ㄱ에 가까운)을 기준으로 정렬
     * 1차 정렬: 상품명 (ASC/DESC)
     * 2차 정렬: 주문 날짜 (최신순)
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN o.orderItems oi " +
            "WHERE o.user = :user " +
            "AND o.status IN (com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED, " +
            "                 com.back.domain.order.order.entity.OrderStatus.PREPARING_SHIPMENT, " +
            "                 com.back.domain.order.order.entity.OrderStatus.SHIPPING, " +
            "                 com.back.domain.order.order.entity.OrderStatus.DELIVERED) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     EXISTS (SELECT 1 FROM OrderItem oi2 WHERE oi2.order = o AND oi2.product.name LIKE CONCAT('%', :keyword, '%'))) " +
            "AND oi.product.name = (SELECT MIN(oi3.product.name) FROM OrderItem oi3 WHERE oi3.order = o) " +
            "ORDER BY " +
            "CASE WHEN :direction = 'ASC' THEN oi.product.name END ASC, " +
            "CASE WHEN :direction = 'DESC' THEN oi.product.name END DESC, " +
            "o.orderDate DESC")
    Page<Order> findOrdersForDashboardSortedByProductName(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("direction") String direction,
            Pageable pageable
    );

    /**
     * 주문 상세 정보 조회 (OrderItem, Product 포함)
     * 대시보드에서 페이징 후 실제 데이터를 가져올 때 사용
     * Product의 Images는 @BatchSize로 최적화 (N+1 방지)
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE o.id IN :orderIds")
    List<Order> findOrdersWithDetailsById(@Param("orderIds") List<Long> orderIds);

    /**
     * 작가별 주문 목록 조회 (작가용 대시보드)
     * 작가가 판매한 상품의 주문만 조회
     * 주문 상태, 키워드 검색, 날짜 범위 필터 지원
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     o.user.name LIKE CONCAT('%', :keyword, '%') OR " +
            "     p.name LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findOrdersByArtist(
            @Param("artistId") Long artistId,
            @Param("status") OrderStatus status,
            @Param("keyword") String keyword,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 작가별 주문 상세 정보 조회 (Fetch Join)
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH o.user " +
            "WHERE o.id IN :orderIds " +
            "AND EXISTS (SELECT 1 FROM OrderItem oi2 JOIN oi2.product p2 WHERE oi2.order = o AND p2.user.id = :artistId)")
    List<Order> findOrdersWithDetailsByArtist(@Param("orderIds") List<Long> orderIds, @Param("artistId") Long artistId);

    /**
     * 작가 대시보드 통계 조회 (한 번의 쿼리로 모든 통계)
     * - 오늘의 주문 건수, 오늘의 매출
     * - 총 주문 건수, 총 매출
     */
    @Query("SELECT new com.back.domain.dashboard.artist.dto.DashboardStatsDto(" +
            "CAST(SUM(CASE WHEN o.orderDate >= :startOfDay AND o.orderDate < :endOfDay THEN 1 ELSE 0 END) AS long), " +
            "CAST(COALESCE(SUM(CASE WHEN o.orderDate >= :startOfDay AND o.orderDate < :endOfDay THEN o.finalAmount ELSE 0 END), 0) AS long), " +
            "COUNT(o.id), " +
            "CAST(COALESCE(SUM(o.finalAmount), 0) AS long)) " +
            "FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND o.status = com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED")
    com.back.domain.dashboard.artist.dto.DashboardStatsDto getArtistDashboardStats(
            @Param("artistId") Long artistId,
            @Param("startOfDay") java.time.LocalDateTime startOfDay,
            @Param("endOfDay") java.time.LocalDateTime endOfDay
    );

    /**
     * 작가별 일별 트렌드 조회 (매출 + 주문 수)
     * - 기간 내 일별 집계
     * - 1M, 3M, 6M, 1Y, ALL 지원
     */
    @Query("SELECT new com.back.domain.dashboard.artist.dto.DailyTrendDto(" +
            "CAST(o.orderDate AS LocalDate), " +
            "COUNT(o.id), " +
            "CAST(COALESCE(SUM(o.finalAmount), 0) AS long)) " +
            "FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND o.orderDate >= :startDate " +
            "AND o.orderDate < :endDate " +
            "AND o.status = com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED " +
            "GROUP BY CAST(o.orderDate AS LocalDate) " +
            "ORDER BY CAST(o.orderDate AS LocalDate) ASC")
    List<com.back.domain.dashboard.artist.dto.DailyTrendDto> findDailyTrendsByArtist(
            @Param("artistId") Long artistId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}