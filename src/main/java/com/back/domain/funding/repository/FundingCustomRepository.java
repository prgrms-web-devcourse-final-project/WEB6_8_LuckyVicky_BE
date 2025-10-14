package com.back.domain.funding.repository;

import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface FundingCustomRepository {

    /**
     * 펀딩 목록 조회 (필터링 + 정렬 + 페이징)
     * @param statuses 펀딩 상태 목록 (null이면 전체)
     * @param keyword 제목 검색어 (null이면 전체)
     * @param minPrice 최소 목표 금액 (null이면 제한 없음)
     * @param maxPrice 최대 목표 금액 (null이면 제한 없음)
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 펀딩 목록
     */
    Page<Funding> findByFilters(
            Set<FundingStatus> statuses,
            String keyword,
            Long minPrice,
            Long maxPrice,
            Pageable pageable
    );

    // 검색(펀딩)
    List<Funding> searchByTitle(String keyword);
}