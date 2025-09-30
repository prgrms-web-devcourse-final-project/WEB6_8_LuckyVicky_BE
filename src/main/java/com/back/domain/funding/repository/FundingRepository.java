package com.back.domain.funding.repository;

import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface FundingRepository extends JpaRepository<Funding, Long>, JpaSpecificationExecutor<Funding> {


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

    // 상태 + 제목으로 펀딩 검색
    @EntityGraph(attributePaths = {"user"})
    Page<Funding> findByStatusInAndTitleContainingIgnoreCase(
            Set<FundingStatus> statuses, String title, Pageable pageable);

    // 상태별 펀딩 목록 조회
    @EntityGraph(attributePaths = {"user"})
    Page<Funding> findByStatusIn(Set<FundingStatus> statuses, Pageable pageable);

    // 제목으로 펀딩 검색
    @EntityGraph(attributePaths = {"user"})
    Funding findByTitle(String title);

    // 사용자별 펀딩 목록
    @EntityGraph(attributePaths = {"user"})
    Page<Funding> findByUserId(Long userId, Pageable pageable);

    // 마감 임박순 펀딩 조회
    @EntityGraph(attributePaths = {"user"})
    Page<Funding> findByStatusOrderByEndDateAsc(FundingStatus status, Pageable pageable);

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
}

