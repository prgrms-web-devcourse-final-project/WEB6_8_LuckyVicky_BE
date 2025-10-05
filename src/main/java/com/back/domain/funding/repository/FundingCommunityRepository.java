package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingCommunity;
import org.springframework.data.jpa.repository.JpaRepository;

// 펀딩 댓글/커뮤니티 관리
public interface FundingCommunityRepository extends JpaRepository<FundingCommunity, Long> {

}
