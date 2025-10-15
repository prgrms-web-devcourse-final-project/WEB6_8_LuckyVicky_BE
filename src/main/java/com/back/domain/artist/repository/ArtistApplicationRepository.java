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
    long countByStatus(ApplicationStatus status);                                       // 대시보드 통계용 - 특정 상태의 작가 신청서 개수 조회

    // userId로 조회
    Optional<ArtistApplication> findByUserId(Long userId);

    // ==== 관리자 대시보드용 - 동적 정렬 지원 ==== //
    /**
     * 관리자 입점 신청 목록 조회 (검색 + 필터링 + 동적 정렬)
     * 
     * 기능:
     * - 작가명, 이메일, 작가ID로 검색 (keyword)
     * - 상태별 필터링 (status)
     * - 작가ID, 작가명, 신청일자, 상태로 정렬 (sort, order)
     * 
     * @param keyword 검색어 (작가명/이메일/작가ID)
     * @param status 신청 상태 (PENDING/APPROVED/REJECTED/CANCELLED)
     * @param sort 정렬 기준 (artistId/artistName/submittedAt/status)
     * @param order 정렬 순서 (ASC/DESC)
     * @param pageable 페이징 정보
     * @return 입점 신청 목록
     */
    @Query("""
        SELECT a FROM ArtistApplication a
        LEFT JOIN FETCH a.user u
        WHERE (:keyword IS NULL OR 
               LOWER(a.artistName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               CAST(u.id AS string) LIKE CONCAT('%', :keyword, '%'))
        AND (:status IS NULL OR a.status = :status)
        ORDER BY 
            CASE WHEN :sort = 'artistId' AND :order = 'ASC' THEN u.id END ASC,
            CASE WHEN :sort = 'artistId' AND :order = 'DESC' THEN u.id END DESC,
            CASE WHEN :sort = 'artistName' AND :order = 'ASC' THEN a.artistName END ASC,
            CASE WHEN :sort = 'artistName' AND :order = 'DESC' THEN a.artistName END DESC,
            CASE WHEN :sort = 'submittedAt' AND :order = 'ASC' THEN a.createDate END ASC,
            CASE WHEN :sort = 'submittedAt' AND :order = 'DESC' THEN a.createDate END DESC,
            CASE WHEN :sort = 'status' AND :order = 'ASC' THEN a.status END ASC,
            CASE WHEN :sort = 'status' AND :order = 'DESC' THEN a.status END DESC,
            a.createDate DESC
        """)
    Page<ArtistApplication> findArtistApplicationsForAdmin(
            @Param("keyword") String keyword,
            @Param("status") ApplicationStatus status,
            @Param("sort") String sort,
            @Param("order") String order,
            Pageable pageable
    );
}
