package com.back.domain.funding.repository;

import com.back.domain.funding.entity.FundingWish;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FundingWishRepository extends JpaRepository<FundingWish, Long> {

    // 특정 사용자의 특정 펀딩 찜 여부 확인
    boolean existsByUserIdAndFundingId(Long userId, Long fundingId);

    // 특정 사용자의 특정 펀딩 찜 조회
    Optional<FundingWish> findByUserIdAndFundingId(Long userId, Long fundingId);

    // 특정 사용자의 찜 목록 조회 (페이징)
    @Query("SELECT fw FROM FundingWish fw " +
            "JOIN FETCH fw.funding f " +
            "WHERE fw.user.id = :userId " +
            "ORDER BY fw.createDate DESC")
    Page<FundingWish> findByUserIdWithFunding(@Param("userId") Long userId, Pageable pageable);
}