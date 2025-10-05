package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingNews;
import org.springframework.data.jpa.repository.JpaRepository;

// 펀딩 업데이트 관리
public interface FundingNewsRepository extends JpaRepository<FundingNews, Long> {

}
