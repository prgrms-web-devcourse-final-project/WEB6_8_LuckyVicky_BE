package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 펀딩 옵션 관리
public interface FundingOptionRepository extends JpaRepository<FundingOption, Long> {

    // 펀딩별 옵션 목록 (정렬순)
    List<FundingOption> findByFundingIdOrderBySortOrderAscIdAsc(Long fundingId);

}
