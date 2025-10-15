package com.back.domain.order.exchange.repository;

import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.order.entity.Order;
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
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    // 주문별 교환 조회
    List<Exchange> findByOrder(Order order);
    
    // 사용자별 교환 조회
    List<Exchange> findByUser(User user);
    
    // 상태별 교환 조회
    List<Exchange> findByStatus(Exchange.ExchangeStatus status);
    
    // 사용자별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByUserOrderByCreateDateDesc(User user, Pageable pageable);
    
    // 주문별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByOrderOrderByCreateDateDesc(Order order, Pageable pageable);
    
    // 상태별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByStatusOrderByCreateDateDesc(Exchange.ExchangeStatus status, Pageable pageable);
    
    // N+1 문제 해결: 교환과 교환 아이템을 함께 조회
    @Query("SELECT e FROM Exchange e LEFT JOIN FETCH e.exchangeItems ei LEFT JOIN FETCH ei.orderItem oi LEFT JOIN FETCH oi.product p LEFT JOIN FETCH p.images WHERE e.id = :exchangeId")
    Optional<Exchange> findByIdWithItems(@Param("exchangeId") Long exchangeId);
    
    // N+1 문제 해결: 사용자별 교환 목록과 아이템들을 함께 조회
    @Query("SELECT e FROM Exchange e LEFT JOIN FETCH e.exchangeItems ei LEFT JOIN FETCH ei.orderItem oi LEFT JOIN FETCH oi.product p LEFT JOIN FETCH p.images WHERE e.user = :user ORDER BY e.createDate DESC")
    List<Exchange> findByUserWithItems(@Param("user") User user);

    /**
     * 작가의 상품에 대한 교환 요청 조회 (검색 + DB 정렬)
     * - 상품명 정렬 (ASC)
     */
    @Query(value = "SELECT DISTINCT e FROM Exchange e " +
            "JOIN FETCH e.order o " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH e.exchangeItems ei " +
            "LEFT JOIN FETCH ei.orderItem oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
            "     u.name LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.name ASC",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Exchange e " +
                    "JOIN e.exchangeItems ei " +
                    "JOIN ei.orderItem oi " +
                    "JOIN oi.product p " +
                    "WHERE p.user.id = :artistId " +
                    "AND (:status IS NULL OR e.status = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
                    "     e.user.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Exchange> findExchangesByArtistSortedByProductNameAsc(
            @Param("artistId") Long artistId,
            @Param("status") Exchange.ExchangeStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 작가의 상품에 대한 교환 요청 조회 (검색 + DB 정렬)
     * - 상품명 정렬 (DESC)
     */
    @Query(value = "SELECT DISTINCT e FROM Exchange e " +
            "JOIN FETCH e.order o " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH e.exchangeItems ei " +
            "LEFT JOIN FETCH ei.orderItem oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
            "     u.name LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.name DESC",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Exchange e " +
                    "JOIN e.exchangeItems ei " +
                    "JOIN ei.orderItem oi " +
                    "JOIN oi.product p " +
                    "WHERE p.user.id = :artistId " +
                    "AND (:status IS NULL OR e.status = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
                    "     e.user.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Exchange> findExchangesByArtistSortedByProductNameDesc(
            @Param("artistId") Long artistId,
            @Param("status") Exchange.ExchangeStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 작가의 상품에 대한 교환 요청 조회 (검색 + DB 정렬)
     * - 구매자 이름 정렬 (ASC)
     */
    @Query(value = "SELECT DISTINCT e FROM Exchange e " +
            "JOIN FETCH e.order o " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH e.exchangeItems ei " +
            "LEFT JOIN FETCH ei.orderItem oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
            "     u.name LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY u.name ASC",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Exchange e " +
                    "JOIN e.exchangeItems ei " +
                    "JOIN ei.orderItem oi " +
                    "JOIN oi.product p " +
                    "WHERE p.user.id = :artistId " +
                    "AND (:status IS NULL OR e.status = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
                    "     e.user.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Exchange> findExchangesByArtistSortedByCustomerNameAsc(
            @Param("artistId") Long artistId,
            @Param("status") Exchange.ExchangeStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 작가의 상품에 대한 교환 요청 조회 (검색 + DB 정렬)
     * - 구매자 이름 정렬 (DESC)
     */
    @Query(value = "SELECT DISTINCT e FROM Exchange e " +
            "JOIN FETCH e.order o " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH e.exchangeItems ei " +
            "LEFT JOIN FETCH ei.orderItem oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
            "     u.name LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY u.name DESC",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Exchange e " +
                    "JOIN e.exchangeItems ei " +
                    "JOIN ei.orderItem oi " +
                    "JOIN oi.product p " +
                    "WHERE p.user.id = :artistId " +
                    "AND (:status IS NULL OR e.status = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
                    "     e.user.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Exchange> findExchangesByArtistSortedByCustomerNameDesc(
            @Param("artistId") Long artistId,
            @Param("status") Exchange.ExchangeStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 작가의 상품에 대한 교환 요청 조회 (검색 + 기본 정렬)
     * - 주문일자, 상태 정렬 (Pageable로 처리)
     */
    @Query(value = "SELECT DISTINCT e FROM Exchange e " +
            "JOIN FETCH e.order o " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH e.exchangeItems ei " +
            "LEFT JOIN FETCH ei.orderItem oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE p.user.id = :artistId " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
            "     u.name LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Exchange e " +
                    "JOIN e.exchangeItems ei " +
                    "JOIN ei.orderItem oi " +
                    "JOIN oi.product p " +
                    "WHERE p.user.id = :artistId " +
                    "AND (:status IS NULL OR e.status = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "     p.name LIKE CONCAT('%', :keyword, '%') OR " +
                    "     e.user.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Exchange> findExchangesByArtist(
            @Param("artistId") Long artistId,
            @Param("status") Exchange.ExchangeStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
