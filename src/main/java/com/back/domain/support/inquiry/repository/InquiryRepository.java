package com.back.domain.support.inquiry.repository;

import com.back.domain.support.inquiry.entity.Inquiry;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 특정 사용자의 문의 목록 조회
    @EntityGraph(attributePaths = {"replies"})
    Page<Inquiry> findByUserOrderByCreateDateDesc(User user, Pageable pageable);

    // 전체 문의 목록 조회 (관리자용)
    @EntityGraph(attributePaths = {"replies"})
    Page<Inquiry> findAllByOrderByCreateDateDesc(Pageable pageable);

    // 공개 문의 목록 조회 (비밀문의 제외)
    @EntityGraph(attributePaths = {"replies"})
    @Query("SELECT i FROM Inquiry i " +
            "WHERE i.isSecret = false " +
            "ORDER BY i.createDate DESC")
    Page<Inquiry> findPublicInquiries(Pageable pageable);

    // 공개 문의 + 본인 비밀문의 조회
    @EntityGraph(attributePaths = {"replies"})
    @Query("SELECT i FROM Inquiry i " +
            "WHERE i.isSecret = false " +
            "OR i.user.id = :userId " +
            "ORDER BY i.createDate DESC")
    Page<Inquiry> findPublicInquiriesOrMine(@Param("userId") Long userId, Pageable pageable);

    // 문의 상세 조회 (첨부파일 + 댓글 포함)
    @Query("SELECT i FROM Inquiry i " +
            "LEFT JOIN FETCH i.documents " +
            "WHERE i.id = :inquiryId")
    Optional<Inquiry> findByIdWithDetails(@Param("inquiryId") Long inquiryId);

}
