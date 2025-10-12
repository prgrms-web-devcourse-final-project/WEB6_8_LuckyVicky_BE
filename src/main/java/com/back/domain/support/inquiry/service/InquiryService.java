package com.back.domain.support.inquiry.service;

import com.back.domain.support.inquiry.dto.request.InquiryCreateRequest;
import com.back.domain.support.inquiry.dto.request.InquiryReplyRequest;
import com.back.domain.support.inquiry.dto.request.InquiryUpdateRequest;
import com.back.domain.support.inquiry.dto.response.InquiryDetailResponse;
import com.back.domain.support.inquiry.dto.response.InquiryListResponse;
import com.back.domain.support.inquiry.entity.*;
import com.back.domain.support.inquiry.repository.InquiryDocumentRepository;
import com.back.domain.support.inquiry.repository.InquiryReplyRepository;
import com.back.domain.support.inquiry.repository.InquiryRepository;
import com.back.domain.user.entity.User;
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
 * 문의 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryDocumentRepository inquiryDocumentRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final S3Service s3Service;

    /**
     * 문의 생성
     */
    @Transactional
    public Long createInquiry(InquiryCreateRequest request, User user) {
        // 1. 첨부파일 개수 검증
        if (request.files() != null && request.files().size() > 3) {
            throw new ServiceException("400", "첨부파일은 최대 3개까지 가능합니다.");
        }

        // 2. 문의 엔티티 생성
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .isSecret(request.isSecret())
                .build();

        // 3. 문의 저장
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        // 4. 첨부파일 처리
        if (request.files() != null && !request.files().isEmpty()) {
            uploadAndSaveDocuments(savedInquiry, request.files());
        }

        log.info("문의 생성 완료 - inquiryId: {}, userId: {}", savedInquiry.getId(), user.getId());
        return savedInquiry.getId();
    }

    /**
     * 내 문의 목록 조회 (일반 사용자 - 본인 문의만)
     */
    @Transactional(readOnly = true)
    public InquiryListResponse getMyInquiries(User user, Pageable pageable) {
        log.info("내 문의 목록 조회 - userId: {}, page: {}", user.getId(), pageable.getPageNumber());

        Page<Inquiry> inquiryPage = inquiryRepository.findByUserOrderByCreateDateDesc(user, pageable);
        return InquiryListResponse.from(inquiryPage);
    }

    /**
     * 공개 문의 목록 조회 (일반 사용자 - 비로그인 상태)
     * - 비밀문의 제외
     */
    @Transactional(readOnly = true)
    public InquiryListResponse getPublicInquiries(Pageable pageable) {
        log.info("공개 문의 목록 조회 (비로그인) - page: {}", pageable.getPageNumber());

        Page<Inquiry> inquiryPage = inquiryRepository.findPublicInquiries(pageable);
        return InquiryListResponse.from(inquiryPage);
    }

    /**
     * 공개 문의 + 내 문의 목록 조회 (일반 사용자 - 로그인 상태)
     * - 공개 문의 + 본인의 비밀문의만 조회
     */
    @Transactional(readOnly = true)
    public InquiryListResponse getPublicInquiriesOrMine(User user, Pageable pageable) {
        log.info("공개 문의 + 내 문의 목록 조회 - userId: {}, page: {}", user.getId(), pageable.getPageNumber());

        Page<Inquiry> inquiryPage = inquiryRepository.findPublicInquiriesOrMine(user.getId(), pageable);
        return InquiryListResponse.from(inquiryPage);
    }

    /**
     * 전체 문의 목록 조회 (관리자 전용)
     * - 비밀문의 포함
     */
    @Transactional(readOnly = true)
    public InquiryListResponse getAllInquiriesForAdmin(Pageable pageable) {
        log.info("전체 문의 목록 조회 (관리자) - page: {}", pageable.getPageNumber());

        Page<Inquiry> inquiryPage = inquiryRepository.findAllByOrderByCreateDateDesc(pageable);
        return InquiryListResponse.from(inquiryPage);
    }

    /**
     * 문의 상세 조회
     */
    @Transactional
    public InquiryDetailResponse getInquiry(Long inquiryId, Long currentUserId, boolean isAdmin) {
        log.info("문의 상세 조회 - inquiryId: {}, userId: {}, isAdmin: {}", inquiryId, currentUserId, isAdmin);

        // 1. 문의 조회
        Inquiry inquiry = inquiryRepository.findByIdWithDetails(inquiryId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 문의입니다."));

        // 2. 비밀문의 접근 권한 체크
        if (!inquiry.canAccess(currentUserId, isAdmin)) {
            throw new ServiceException("403", "비밀문의는 작성자와 관리자만 조회할 수 있습니다.");
        }

        // 3. 조회수 증가
        inquiry.increaseViewCount();

        return InquiryDetailResponse.from(inquiry);
    }

    /**
     * 문의 수정
     */
    @Transactional
    public void updateInquiry(Long inquiryId, InquiryUpdateRequest request, Long currentUserId) {
        log.info("문의 수정 - inquiryId: {}, userId: {}", inquiryId, currentUserId);

        // 1. 문의 조회
        Inquiry inquiry = inquiryRepository.findByIdWithDetails(inquiryId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 문의입니다."));

        // 2. 작성자 본인 확인
        if (!inquiry.isWrittenBy(currentUserId)) {
            throw new ServiceException("403", "본인의 문의만 수정할 수 있습니다.");
        }

        // 3. 첨부파일 개수 검증
        int existingFileCount = inquiry.getDocuments().size();
        int deleteFileCount = request.deleteFileIds() != null ? request.deleteFileIds().size() : 0;
        int newFileCount = request.files() != null ? request.files().size() : 0;

        if (existingFileCount - deleteFileCount + newFileCount > 3) {
            throw new ServiceException("400", "첨부파일은 최대 3개까지 가능합니다.");
        }

        // 4. 기본 정보 수정
        inquiry.update(request.title(), request.content(), request.category(), request.isSecret());

        // 5. 기존 첨부파일 삭제 처리
        if (request.deleteFileIds() != null && !request.deleteFileIds().isEmpty()) {
            deleteDocuments(inquiry, request.deleteFileIds());
        }

        // 6. 새 첨부파일 업로드 및 저장
        if (request.files() != null && !request.files().isEmpty()) {
            uploadAndSaveDocuments(inquiry, request.files());
        }

        log.info("문의 수정 완료 - inquiryId: {}", inquiryId);
    }

    /**
     * 문의 삭제
     */
    @Transactional
    public void deleteInquiry(Long inquiryId, Long currentUserId, boolean isAdmin) {
        log.info("문의 삭제 - inquiryId: {}, userId: {}, isAdmin: {}", inquiryId, currentUserId, isAdmin);

        // 1. 문의 조회
        Inquiry inquiry = inquiryRepository.findByIdWithDetails(inquiryId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 문의입니다."));

        // 2. 권한 확인 (작성자 본인 또는 관리자)
        if (!inquiry.isWrittenBy(currentUserId) && !isAdmin) {
            throw new ServiceException("403", "본인의 문의만 삭제할 수 있습니다.");
        }

        // 3. S3에서 첨부파일 삭제
        for (InquiryDocument document : inquiry.getDocuments()) {
            try {
                s3Service.deleteFile(document.getS3Key());
                log.info("S3 파일 삭제 완료 - s3Key: {}", document.getS3Key());
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패 - s3Key: {}, error: {}", document.getS3Key(), e.getMessage());
            }
        }

        // 4. 문의 삭제 (첨부파일, 댓글도 자동 삭제 - orphanRemoval = true)
        inquiryRepository.delete(inquiry);

        log.info("문의 삭제 완료 - inquiryId: {}", inquiryId);
    }

    // ===== 댓글 CRUD ===== //

    /**
     * 댓글 작성
     */
    @Transactional
    public Long createReply(Long inquiryId, InquiryReplyRequest request, User user, boolean isAdmin) {
        log.info("댓글 작성 - inquiryId: {}, userId: {}, isAdmin: {}", inquiryId, user.getId(), isAdmin);

        // 1. 문의 조회
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 문의입니다."));

        // 2. 비밀문의 접근 권한 체크
        if (!inquiry.canAccess(user.getId(), isAdmin)) {
            throw new ServiceException("403", "비밀문의는 작성자와 관리자만 댓글을 작성할 수 있습니다.");
        }

        // 3. 부모 댓글 확인 (대댓글인 경우)
        InquiryReply parentReply = null;
        if (request.parentReplyId() != null) {
            parentReply = inquiryReplyRepository.findById(request.parentReplyId())
                    .orElseThrow(() -> new ServiceException("404", "존재하지 않는 댓글입니다."));
        }

        // 4. 댓글 타입 결정
        ReplyType replyType = isAdmin ? ReplyType.ADMIN : ReplyType.USER;

        // 5. 댓글 생성
        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .user(user)
                .content(request.content())
                .replyType(replyType)
                .parentReply(parentReply)
                .build();

        InquiryReply savedReply = inquiryReplyRepository.save(reply);

        // 6. 첫 관리자 답변 시 문의 상태 변경
        if (isAdmin && parentReply == null) {
            inquiry.changeStatus(InquiryStatus.ANSWERED);
            log.info("문의 상태 변경 - inquiryId: {}, status: ANSWERED", inquiryId);
        }

        log.info("댓글 작성 완료 - replyId: {}, inquiryId: {}", savedReply.getId(), inquiryId);
        return savedReply.getId();
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateReply(Long replyId, InquiryReplyRequest request, Long currentUserId) {
        log.info("댓글 수정 - replyId: {}, userId: {}", replyId, currentUserId);

        // 1. 댓글 조회
        InquiryReply reply = inquiryReplyRepository.findById(replyId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 댓글입니다."));

        // 2. 작성자 본인 확인
        if (!reply.isWrittenBy(currentUserId)) {
            throw new ServiceException("403", "본인의 댓글만 수정할 수 있습니다.");
        }

        // 3. 댓글 내용 수정
        reply.update(request.content());

        log.info("댓글 수정 완료 - replyId: {}", replyId);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteReply(Long replyId, Long currentUserId, boolean isAdmin) {
        log.info("댓글 삭제 - replyId: {}, userId: {}, isAdmin: {}", replyId, currentUserId, isAdmin);

        // 1. 댓글 조회
        InquiryReply reply = inquiryReplyRepository.findById(replyId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 댓글입니다."));

        // 2. 권한 확인 (작성자 본인 또는 관리자)
        if (!reply.isWrittenBy(currentUserId) && !isAdmin) {
            throw new ServiceException("403", "본인의 댓글만 삭제할 수 있습니다.");
        }

        // 3. 댓글 삭제 (대댓글도 자동 삭제 - orphanRemoval = true)
        inquiryReplyRepository.delete(reply);

        log.info("댓글 삭제 완료 - replyId: {}", replyId);
    }

    // ===== 헬퍼 메서드 ===== //

    /**
     * 첨부파일 업로드 및 DB 저장
     */
    private void uploadAndSaveDocuments(Inquiry inquiry, List<MultipartFile> files) {
        // 파일 타입 리스트 생성 (모두 DOCUMENT 타입)
        List<FileType> fileTypes = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            fileTypes.add(FileType.DOCUMENT);
        }

        // S3 업로드
        List<UploadResultResponse> uploadResults = s3Service.uploadFiles(files, "inquiry-documents", fileTypes);

        // DB 저장
        for (int i = 0; i < uploadResults.size(); i++) {
            UploadResultResponse result = uploadResults.get(i);

            InquiryDocument document = InquiryDocument.builder()
                    .inquiry(inquiry)
                    .fileName(result.originalFileName())
                    .fileUrl(result.url())
                    .s3Key(result.s3Key())
                    .build();

            inquiry.addDocument(document);
            log.info("첨부파일 저장 완료 - fileName: {}", document.getFileName());
        }
    }

    /**
     * 첨부파일 삭제
     */
    private void deleteDocuments(Inquiry inquiry, List<Long> deleteFileIds) {
        for (Long fileId : deleteFileIds) {
            InquiryDocument document = inquiryDocumentRepository.findById(fileId)
                    .orElseThrow(() -> new ServiceException("404", "존재하지 않는 첨부파일입니다."));

            // 권한 확인 (해당 문의의 첨부파일인지)
            if (!document.belongsTo(inquiry.getId())) {
                throw new ServiceException("403", "해당 문의의 첨부파일이 아닙니다.");
            }

            // S3에서 삭제
            try {
                s3Service.deleteFile(document.getS3Key());
                log.info("S3 파일 삭제 완료 - s3Key: {}", document.getS3Key());
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패 - s3Key: {}, error: {}", document.getS3Key(), e.getMessage());
            }

            // DB에서 삭제
            inquiry.getDocuments().remove(document);
            inquiryDocumentRepository.delete(document);
        }
    }

}
