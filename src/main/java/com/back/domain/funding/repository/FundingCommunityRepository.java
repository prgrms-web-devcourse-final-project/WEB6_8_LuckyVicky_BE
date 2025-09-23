package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingCommunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 펀딩 댓글/커뮤니티 관리
public interface FundingCommunityRepository extends JpaRepository<FundingCommunity, Long> {

    // 펀딩별 댓글 목록 (삭제된 것 제외)
    @EntityGraph(attributePaths = {"funding", "author"})
    List<FundingCommunity> findByFundingIdAndDeletedFalseOrderByCreateDateDesc(Long fundingId);

    // 펀딩별 댓글 목록
    @EntityGraph(attributePaths = {"funding", "author"})
    Page<FundingCommunity> findByFundingIdAndDeletedFalse(Long fundingId, Pageable pageable);

    // 펀딩별 댓글 수
    long countByFundingIdAndDeletedFalse(Long fundingId);

    // 사용자별 댓글 목록
    @EntityGraph(attributePaths = {"funding", "author"})
    Page<FundingCommunity> findByAuthorIdAndDeletedFalseOrderByCreateDateDesc(Long authorId, Pageable pageable);
}
