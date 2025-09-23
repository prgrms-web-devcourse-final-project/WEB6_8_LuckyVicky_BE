package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 펀딩 옵션 관리
public interface FundingOptionRepository extends JpaRepository<FundingOption, Long> {

    // 펀딩별 옵션 목록 (정렬순)
    List<FundingOption> findByFundingIdOrderBySortOrderAscIdAsc(Long fundingId);

    // 펀딩별 옵션 목록 (펀딩 정보 포함)
    @EntityGraph(attributePaths = {"funding"})
    List<FundingOption> findByFundingId(Long fundingId);

    // 재고 있는 옵션만 조회
    List<FundingOption> findByFundingIdAndAvailableQuantityGreaterThanOrderBySortOrderAsc(Long fundingId, int minQty);
}
