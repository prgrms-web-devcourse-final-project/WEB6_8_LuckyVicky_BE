package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 펀딩 업데이트 관리
public interface FundingNewsRepository extends JpaRepository<FundingNews, Long> {

    // 펀딩별 새소식 목록 (최신순)
    @EntityGraph(attributePaths = {"funding", "user"})
    List<FundingNews> findByFundingIdOrderByCreateDateDesc(Long fundingId);

    // 펀딩별 새소식 목록 (페이징)
    @EntityGraph(attributePaths = {"funding", "user"})
    Page<FundingNews> findByFundingIdOrderByCreateDateDesc(Long fundingId, Pageable pageable);
}
