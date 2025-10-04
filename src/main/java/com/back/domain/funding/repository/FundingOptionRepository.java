package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingOption;
import org.springframework.data.jpa.repository.JpaRepository;

// 펀딩 옵션 관리
public interface FundingOptionRepository extends JpaRepository<FundingOption, Long> {
}
