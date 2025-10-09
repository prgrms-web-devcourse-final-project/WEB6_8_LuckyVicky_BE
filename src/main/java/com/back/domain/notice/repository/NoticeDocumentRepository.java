package com.back.domain.notice.repository;

import com.back.domain.notice.entity.NoticeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeDocumentRepository extends JpaRepository<NoticeDocument, Long> {

    // 공지사항 ID로 첨부파일 목록 조회
    List<NoticeDocument> findByNoticeId(Long noticeId);

    // 공지사항 ID로 첨부파일 삭제
    @Modifying
    @Query("DELETE FROM NoticeDocument d WHERE d.notice.id = :noticeId")
    void deleteByNoticeId(@Param("noticeId") Long noticeId);

    // S3 Key로 첨부파일 조회
    List<NoticeDocument> findByS3Key(String s3Key);
}
