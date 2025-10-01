package com.back.domain.artist.repository;

import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistApplicationRepository extends JpaRepository<ArtistApplication, Long> {

    // ==== 사용자용 ==== //
    List<ArtistApplication> findByUserIdOrderByCreateDateDesc(Long userId);              // 마이페이지 - 전체 작가 신청서 조회
    Optional<ArtistApplication> findFirstByUserIdOrderByCreateDateDesc(Long userId);     // 최근 신청 1건 조회
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);             // 중복 신청 방지용 - 특정 상태의 작가 신청서 존재 여부 확인


    // ==== 관리자용 ==== //
    Page<ArtistApplication> findAllByOrderByCreateDateDesc(Pageable pageable);           // 모든 작가 신청서 조회
    Page<ArtistApplication> findByStatusOrderByCreateDateDesc(                           // 상태별 작가 신청서 조회
            ApplicationStatus status, Pageable pageable);
    long countByStatus(ApplicationStatus status);                                       // 대시보드 통계용 - 특정 상태의 작가 신청서 개수 조회

    // 검색
    @Query("SELECT a FROM ArtistApplication a WHERE a.artistName LIKE %:artistName% ORDER BY a.createDate DESC")
    Page<ArtistApplication> findByArtistNameContainingOrderByCreateDateDesc(             // 작가명 검색
            @Param("artistName") String artistName, Pageable pageable
    );

}
