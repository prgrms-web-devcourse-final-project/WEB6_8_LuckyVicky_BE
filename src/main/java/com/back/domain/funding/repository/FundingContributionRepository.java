package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingContribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FundingContributionRepository extends JpaRepository<FundingContribution, Long> {

    @Query("""
           select coalesce(sum(fc.totalAmount), 0)
           from FundingContribution fc
           where fc.funding.id = :fundingId
           """)
    Long sumContributedAmountByFundingId(Long fundingId);

    @Query("""
           select count(distinct fc.buyer.id)
           from FundingContribution fc
           where fc.funding.id = :fundingId
           """)
    Long countDistinctParticipantsByFundingId(Long fundingId);

    /**
     * 특정 펀딩에 참여한 모든 사용자 조회 (알림 발송용)
     */
    @Query("""
           select distinct fc.buyer
           from FundingContribution fc
           where fc.funding.id = :fundingId
           """)
    List<com.back.domain.user.entity.User> findAllParticipantsByFundingId(@Param("fundingId") Long fundingId);
    
    /**
     * 고객 대시보드용 - 사용자가 참여한 펀딩 목록 조회 (검색 + 필터링)
     * 정렬은 Pageable에서 처리
     * 상태 필터링: 8가지 FundingStatus를 직접 필터링
     */
    @Query(value = """
        SELECT fc FROM FundingContribution fc
        JOIN FETCH fc.funding f
        JOIN FETCH f.user u
        WHERE fc.buyer.id = :userId
        AND (COALESCE(:keyword, '') = '' OR
             LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (COALESCE(:status, '') = '' OR
             CAST(f.status AS string) = :status)
        """,
        countQuery = """
        SELECT COUNT(fc) FROM FundingContribution fc
        JOIN fc.funding f
        JOIN f.user u
        WHERE fc.buyer.id = :userId
        AND (COALESCE(:keyword, '') = '' OR
             LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (COALESCE(:status, '') = '' OR
             CAST(f.status AS string) = :status)
        """)
    Page<FundingContribution> findContributionsByBuyerWithFilters(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}
