package com.back.domain.funding.repository;

import com.back.domain.funding.entity.*;
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
    Funding findByTitleContainingIgnoreCase(String title);

    // 사용자별 펀딩 목록
    @EntityGraph(attributePaths = {"user"})
    Page<Funding> findByUserId(Long userId, Pageable pageable);

    // 마감 임박순 펀딩 조회
    @EntityGraph(attributePaths = {"user"})
    Page<Funding> findByStatusOrderByEndDateAsc(FundingStatus status, Pageable pageable);
}

