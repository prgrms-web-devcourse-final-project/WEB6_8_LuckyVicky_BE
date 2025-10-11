package com.back.domain.support.faq.repository;

import com.back.domain.support.faq.entity.Faq;
import com.back.domain.support.faq.entity.FaqCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    // 전체 목록 조회 (생성일 기준 최신순)
    Page<Faq> findAllByOrderByCreateDateDesc(Pageable pageable);

    // 카테고리별 목록 조회 (생성일 기준 최신순)
    Page<Faq> findByCategoryOrderByCreateDateDesc(
            FaqCategory category, Pageable pageable);

    // 조회수 기준 상위 10개 조회
    List<Faq> findTop10ByOrderByViewCountDesc();
}
