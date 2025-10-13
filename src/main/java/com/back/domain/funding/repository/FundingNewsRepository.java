package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingNewsRepository extends JpaRepository<FundingNews, Long> {
}
