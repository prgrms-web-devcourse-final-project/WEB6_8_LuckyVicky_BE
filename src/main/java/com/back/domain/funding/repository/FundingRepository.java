package com.back.domain.funding.repository;

import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FundingRepository extends JpaRepository<Funding, Long>, JpaSpecificationExecutor<Funding>, FundingCustomRepository {


    // 펀딩 상세 조회 (작성자 + 옵션 포함)
    @Query("""
        SELECT DISTINCT f
        FROM Funding f
        LEFT JOIN FETCH f.user
        LEFT JOIN FETCH f.options o
        WHERE f.id = :id
        ORDER BY o.sortOrder ASC, o.id ASC
        """)
    Optional<Funding> findByIdWithUserAndOptions(@Param("id") Long id);

    // 작가 대시보드용 - 작가별 펀딩 목록 조회 (검색 + 필터링 + 정렬)
    @Query("""
        SELECT f FROM Funding f
        WHERE f.user.id = :userId
        AND (:keyword IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:status IS NULL OR f.status = :status)
        ORDER BY 
            CASE WHEN :sort = 'endDate' AND :order = 'ASC' THEN f.endDate END ASC,
            CASE WHEN :sort = 'endDate' AND :order = 'DESC' THEN f.endDate END DESC,
            CASE WHEN :sort = 'createDate' AND :order = 'ASC' THEN f.createDate END ASC,
            CASE WHEN :sort = 'createDate' AND :order = 'DESC' THEN f.createDate END DESC,
            CASE WHEN :sort = 'title' AND :order = 'ASC' THEN f.title END ASC,
            CASE WHEN :sort = 'title' AND :order = 'DESC' THEN f.title END DESC,
            f.createDate DESC
        """)
    Page<Funding> findFundingsByArtist(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") FundingStatus status,
            @Param("sort") String sort,
            @Param("order") String order,
            Pageable pageable
    );

    // 시작일이 도래한 승인된 펀딩 카운트
    @Query("SELECT COUNT(f) FROM Funding f " +
            "WHERE f.status = :status AND f.startDate <= :now")
    long countByStatusAndStartDateBefore(
            @Param("status") FundingStatus status,
            @Param("now") LocalDateTime now
    );

    // 승인된 펀딩 일괄 오픈
    @Modifying
    @Transactional
    @Query("UPDATE Funding f SET f.status = 'OPEN' " +
            "WHERE f.status = 'APPROVED' AND f.startDate <= :now")
    int bulkOpenApprovedFundings(@Param("now") LocalDateTime now);

    // 시작일이 도래한 승인된 펀딩 조회 (정합성 체크용)
    List<Funding> findByStatusAndStartDateBefore(
            FundingStatus status,
            LocalDateTime startDate
    );

    // 종료일이 지난 펀딩 조회
    @Query("SELECT COUNT(f) FROM Funding f " +
            "WHERE f.status = :status AND f.endDate < :now")
    long countByStatusAndEndDateBefore(
            @Param("status") FundingStatus status,
            @Param("now") LocalDateTime now
    );

    // 만료된 펀딩 일괄 종료
    @Modifying
    @Transactional
    @Query("UPDATE Funding f SET f.status = 'CLOSED' " +
            "WHERE f.status = 'OPEN' AND f.endDate < :now")
    int bulkCloseExpiredFundings(@Param("now") LocalDateTime now);

    // 특정 상태의 펀딩 전체 조회
    List<Funding> findByStatus(FundingStatus status);

    // 종료된 펀딩 조회 (정합성 체크용)
    List<Funding> findByStatusAndEndDateBefore(
            FundingStatus status,
            LocalDateTime endDate
    );

    // 관리자 대시보드 - 펀딩 승인 대기 목록 조회 (PENDING 상태 펀딩)
    @Query("""
        SELECT f FROM Funding f
        LEFT JOIN FETCH f.user u
        WHERE f.status = 'PENDING'
        AND (:keyword IS NULL OR 
             LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             CAST(u.id AS string) LIKE CONCAT('%', :keyword, '%'))
        AND (:artistId IS NULL OR f.user.id = :artistId)
        ORDER BY 
            CASE WHEN :sort = 'artistId' AND :order = 'ASC' THEN u.id END ASC,
            CASE WHEN :sort = 'artistId' AND :order = 'DESC' THEN u.id END DESC,
            CASE WHEN :sort = 'artistName' AND :order = 'ASC' THEN u.name END ASC,
            CASE WHEN :sort = 'artistName' AND :order = 'DESC' THEN u.name END DESC,
            CASE WHEN :sort = 'title' AND :order = 'ASC' THEN f.title END ASC,
            CASE WHEN :sort = 'title' AND :order = 'DESC' THEN f.title END DESC,
            CASE WHEN :sort = 'registeredAt' AND :order = 'ASC' THEN f.createDate END ASC,
            CASE WHEN :sort = 'registeredAt' AND :order = 'DESC' THEN f.createDate END DESC,
            f.createDate DESC
        """)
    Page<Funding> findPendingApprovalFundings(
            @Param("keyword") String keyword,
            @Param("artistId") Long artistId,
            @Param("sort") String sort,
            @Param("order") String order,
            Pageable pageable
    );

    // 관리자 대시보드 - 펀딩 승인 대기 상세 조회 (PENDING 상태 + User + ArtistApplication 조인)
    @Query("""
        SELECT f FROM Funding f
        LEFT JOIN FETCH f.user u
        WHERE f.id = :fundingId
        AND f.status = 'PENDING'
        """)
    Optional<Funding> findPendingApprovalFundingById(@Param("fundingId") Long fundingId);

    // 관리자 대시보드 - 특정 상태의 펀딩 목록 조회 (최근 생성순)
    Page<Funding> findByStatusOrderByCreateDateDesc(FundingStatus status, Pageable pageable);
}

