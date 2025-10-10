package com.back.domain.notice.service;

import com.back.domain.notice.dto.request.NoticeCreateRequest;
import com.back.domain.notice.dto.request.NoticeUpdateRequest;
import com.back.domain.notice.dto.response.NoticeDetailResponse;
import com.back.domain.notice.dto.response.NoticeListResponse;
import com.back.domain.notice.entity.Notice;
import com.back.domain.notice.entity.NoticeDocument;
import com.back.domain.notice.repository.NoticeDocumentRepository;
import com.back.domain.notice.repository.NoticeRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 공지사항 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeDocumentRepository noticeDocumentRepository;
    private final S3Service s3Service;

    /**
     * 공지사항 생성
     */
    public Long createNotice(NoticeCreateRequest request) {
        // 1. 공지사항 엔티티 생성
        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .isImportant(request.isImportant())
                .build();

        // 2. 공지사항 저장
        Notice savedNotice = noticeRepository.save(notice);

        // 3. 첨부파일 처리
        if (request.files() != null && !request.files().isEmpty()) {
            uploadAndSaveDocuments(savedNotice, request.files());
        }

        log.info("공지사항 생성 완료 - noticeId: {}", savedNotice.getId());
        return savedNotice.getId();
    }

    /**
     * 공지사항 목록 조회
     */
    public NoticeListResponse getNotices(String keyword, Pageable pageable) {
        log.info("공지사항 목록 조회 - keyword: {}, page: {}, size: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        Page<Notice> noticePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있으면 검색
            noticePage = noticeRepository.searchByKeyword(keyword, pageable);
        } else {
            // 전체 조회
            noticePage = noticeRepository.findAllByOrderByIsImportantDescCreateDateDesc(pageable);
        }

        return NoticeListResponse.from(noticePage);
    }

    /**
     * 공지사항 상세 조회
     */
    @Transactional
    public NoticeDetailResponse getNotice(Long noticeId) {
        log.info("공지사항 상세 조회 - noticeId: {}", noticeId);

        Notice notice = noticeRepository.findByIdWithDocuments(noticeId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 공지사항입니다."));

        // 조회수 증가
        notice.increaseViewCount();

        return NoticeDetailResponse.from(notice);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {

        // 1. 공지사항 조회
        Notice notice = noticeRepository.findByIdWithDocuments(noticeId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 공지사항입니다."));

        // 2. 기본 정보 수정
        notice.update(request.title(), request.content(), request.isImportant());

        // 3. 기존 첨부파일 삭제 처리
        if (request.deleteFileIds() != null && !request.deleteFileIds().isEmpty()) {
            deleteDocuments(notice, request.deleteFileIds());
        }

        // 4. 새 첨부파일 업로드 및 저장
        if (request.files() != null && !request.files().isEmpty()) {
            uploadAndSaveDocuments(notice, request.files());
        }

        log.info("공지사항 수정 완료 - noticeId: {}", noticeId);
    }

    /**
     * 공지사항 삭제
     */
    @Transactional
    public void deleteNotice(Long noticeId) {
        // 1. 공지사항 조회
        Notice notice = noticeRepository.findByIdWithDocuments(noticeId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 공지사항입니다."));

        // 2. S3에서 첨부파일 삭제
        for (NoticeDocument document : notice.getDocuments()) {
            try {
                s3Service.deleteFile(document.getS3Key());
                log.info("S3 파일 삭제 완료 - s3Key: {}", document.getS3Key());
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패 - s3Key: {}, error: {}", document.getS3Key(), e.getMessage());
            }
        }

        // 3. 공지사항 삭제 (첨부파일도 자동 삭제 - orphanRemoval = true)
        noticeRepository.delete(notice);

        log.info("공지사항 삭제 완료 - noticeId: {}", noticeId);
    }

    // ===== 헬퍼 메서드 ===== //

    /**
     * 첨부파일 업로드 및 DB 저장
     */
    private void uploadAndSaveDocuments(Notice notice, List<MultipartFile> files) {
        // 파일 타입 리스트 생성 (모두 DOCUMENT 타입)
        List<FileType> fileTypes = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            fileTypes.add(FileType.DOCUMENT);
        }

        // S3 업로드
        List<UploadResultResponse> uploadResults = s3Service.uploadFiles(files, "notice-documents", fileTypes);

        // DB 저장
        for (int i = 0; i < uploadResults.size(); i++) {
            UploadResultResponse result = uploadResults.get(i);
            MultipartFile file = files.get(i);

            NoticeDocument document = NoticeDocument.builder()
                    .notice(notice)
                    .fileName(result.originalFileName())
                    .fileUrl(result.url())
                    .s3Key(result.s3Key())
                    .build();

            notice.addDocument(document);
            log.info("첨부파일 저장 완료 - fileName: {}", document.getFileName());
        }
    }

    /**
     * 첨부파일 삭제
     */
    private void deleteDocuments(Notice notice, List<Long> deleteFileIds) {
        for (Long fileId : deleteFileIds) {
            NoticeDocument document = noticeDocumentRepository.findById(fileId)
                    .orElseThrow(() -> new ServiceException("404", "존재하지 않는 첨부파일입니다."));

            // 권한 확인 (해당 공지사항의 첨부파일인지)
            if (!document.belongsTo(notice.getId())) {
                throw new ServiceException("403", "해당 공지사항의 첨부파일이 아닙니다.");
            }

            // S3에서 삭제
            try {
                s3Service.deleteFile(document.getS3Key());
                log.info("S3 파일 삭제 완료 - s3Key: {}", document.getS3Key());
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패 - s3Key: {}, error: {}", document.getS3Key(), e.getMessage());
            }

            // DB에서 삭제
            notice.getDocuments().remove(document);
            noticeDocumentRepository.delete(document);
        }
    }

}
