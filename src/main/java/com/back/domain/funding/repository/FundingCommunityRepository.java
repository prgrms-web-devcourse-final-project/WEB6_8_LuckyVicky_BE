package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingCommunity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingCommunityRepository extends JpaRepository<FundingCommunity, Long> {
}
