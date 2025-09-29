package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}