package com.back.domain.notice.repository;

import com.back.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 전체 공지사항 페이징 조회 (중요공지 먼저, 최신순)
    Page<Notice> findAllByOrderByIsImportantDescCreateDateDesc(Pageable pageable);

    // 키워드로 공지사항 검색 (제목, 내용) - 중요공지 먼저, 최신순
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% OR n.content LIKE %:keyword% " +
            "ORDER BY n.isImportant DESC, n.createDate DESC")
    Page<Notice> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 공지사항 상세 조회 (첨부파일 포함)
    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.documents WHERE n.id = :id")
    Optional<Notice> findByIdWithDocuments(@Param("id") Long id);
}
