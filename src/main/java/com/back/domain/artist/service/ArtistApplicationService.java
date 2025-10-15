package com.back.domain.artist.service;

import com.back.domain.artist.dto.request.ArtistApplicationRequest;
import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.dto.response.ArtistBusinessInfoResponse;
import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistDocument;
import com.back.domain.artist.entity.DocumentType;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistDocumentRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.FileType;
import com.back.global.s3.S3FileRequest;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 사용자용 작가 신청 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistApplicationService {

    private final ArtistApplicationRepository artistApplicationRepository;
    private final ArtistDocumentRepository artistDocumentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final S3Service s3Service;

    /**
     * 작가 신청서 생성
     */
    @Transactional
    public Long createApplication(
            Long userId,
            ArtistApplicationRequest request,
            List<MultipartFile> documentFiles) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));

        // 2. 중복 신청 검증
        validateDuplicateApplication(userId);

        // 3. 필수 서류 파일 검증
        if (documentFiles == null || documentFiles.size() < 2) {
            throw new ServiceException("400", "필수 서류 파일이 누락되었습니다. (최소 2개: 사업자등록증, 통신판매업신고증)");
        }

        // 4. 작가 신청서 엔티티 생성 및 저장
        ArtistApplication savedApplication = createAndSaveApplication(user, request);

        // 5. S3에 서류 파일 업로드
        List<UploadResultResponse> uploadResults = s3Service.uploadFiles(
                documentFiles,
                "artist-documents/" + userId,  // 경로: artist-documents/{userId}/
                documentFiles.stream()
                        .map(file -> FileType.DOCUMENT)  // 서류 타입
                        .collect(Collectors.toList())
        );

        // 6. 업로드된 파일 정보로 ArtistDocument 엔티티 생성
        List<ArtistDocument> documents = new ArrayList<>();
        for (int i = 0; i < uploadResults.size(); i++) {
            UploadResultResponse result = uploadResults.get(i);
            MultipartFile file = documentFiles.get(i);

            // ✅ UploadResultResponse 구조에 맞게 수정
            ArtistDocument document = ArtistDocument.builder()
                    .artistApplication(savedApplication)
                    .documentType(determineDocumentType(file.getOriginalFilename()))
                    .fileName(result.originalFileName())  // ✅ originalFileName
                    .fileUrl(result.url())                // ✅ url
                    .s3Key(result.s3Key())                // ✅ s3Key
                    .build();

            documents.add(document);
        }

        // ✅ DB에 저장
        artistDocumentRepository.saveAll(documents);

        // 7. 필수 서류 존재 여부 확인
        validateRequiredDocuments(documents);

        log.info("작가 신청서 생성 완료: userId={}, applicationId={}, 업로드된 서류: {}",
                userId, savedApplication.getId(), documents.size());

        // 8. 알림 발송 - 모든 관리자에게
        List<User> admins = userRepository.findAllAdmins();
        for (User admin : admins) {
            notificationService.sendNotification(
                    admin,
                    NotificationType.ARTIST_VERIFICATION_REQUEST,
                    user.getName() + "님이 작가 인증을 신청했습니다.",
                    "/admin/artist-applications/" + savedApplication.getId()
            );
        }

        return savedApplication.getId();
    }

    /**
     * 내 신청서 목록 조회
     */
    public List<ArtistApplicationSimpleResponse> getMyApplications(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.");
        }

        List<ArtistApplication> applications =
                artistApplicationRepository.findByUserIdOrderByCreateDateDesc(userId);

        return applications.stream()
                .map(ArtistApplicationSimpleResponse::from)
                .toList();
    }

    /**
     * 내 신청서 상세 조회
     */
    public ArtistApplicationResponse getApplicationById(Long userId, Long applicationId) {
        ArtistApplication application = artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ServiceException("404", "신청서를 찾을 수 없습니다."));

        if (!application.getUser().getId().equals(userId)) {
            throw new ServiceException("403", "본인의 신청서만 조회할 수 있습니다.");
        }

        return ArtistApplicationResponse.from(application);
    }

    /**
     * 작가 신청 취소
     */
    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        ArtistApplication application = getApplicationIfOwner(userId, applicationId);
        validateCancellable(application);
        application.cancel();

        log.info("작가 신청 취소 완료: userId={}, applicationId={}", userId, applicationId);
    }


    // ==== 헬퍼 메서드 ==== //

    /**
     * 신청소 조회 및 소유권 검증
     */
    private ArtistApplication getApplicationIfOwner(Long userId, Long applicationId) {
        ArtistApplication application = artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ServiceException("404", "신청서를 찾을 수 없습니다."));

        if (!application.getUser().getId().equals(userId)) {
            throw new ServiceException("403", "본인의 신청서만 접근할 수 있습니다.");
        }

        return application;
    }

    /**
     * 신청서 취소 가능 여부 검증
     */
    private void validateCancellable(ArtistApplication application) {
        if (!application.isPending()) {
            throw new ServiceException("400", "심사 대기 중인 신청서만 취소할 수 있습니다. 현재 상태: " + application.getStatus());
        }
    }

    /**
     * 중복 신청 검증
     */
    private void validateDuplicateApplication(Long userId) {
        boolean exists = artistApplicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING);

        if (exists) {
            throw new ServiceException("400", "이미 심사 대기 중인 신청서가 있습니다.");
        }
    }

    /**
     * 신청서 엔티티 생성 및 저장
     */
    private ArtistApplication createAndSaveApplication(User user, ArtistApplicationRequest request) {
        ArtistApplication application = ArtistApplication.builder()
                .user(user)
                .ownerName(request.ownerName())
                .email(request.email())
                .phone(request.phone())
                .artistName(request.artistName())
                .businessNumber(request.businessNumber())
                .businessName(request.businessName())
                .businessAddress(request.businessAddress())
                .businessAddressDetail(request.businessAddressDetail())
                .businessZipCode(request.businessZipCode())
                .telecomSalesNumber(request.telecomSalesNumber())
                .snsAccount(request.snsAccount())
                .mainProducts(request.mainProducts())
                .managerPhone(request.managerPhone())
                .bankName(request.bankName())
                .bankAccount(request.bankAccount())
                .accountName(request.accountName())
                .build();

        return artistApplicationRepository.save(application);
    }

    /**
     * 서류 엔티티 생성
     */
    private List<ArtistDocument> createDocuments(
            ArtistApplication application,
            Map<DocumentType, List<S3FileRequest>> documentsMap
    ) {
        List<ArtistDocument> documents = new ArrayList<>();

        for (Map.Entry<DocumentType, List<S3FileRequest>> entry : documentsMap.entrySet()) {
            DocumentType documentType = entry.getKey();
            List<S3FileRequest> files = entry.getValue();

            for (S3FileRequest file : files) {
                ArtistDocument document = ArtistDocument.fromS3FileRequest(
                        application,
                        documentType,
                        file
                );
                documents.add(document);
            }
        }

        return documents;
    }

    /**
     * 작가 사업자 관련 정보 조회
     */
    public ArtistBusinessInfoResponse getBusinessInfo(Long userId) {
        log.info("작가 사업자 정보 조회 - userId: {}", userId);
        ArtistApplication application = artistApplicationRepository.findByUserId(userId)
                .orElseThrow(() -> new ServiceException("404", "작가 신청 정보가 없습니다."));

        return new ArtistBusinessInfoResponse(
                application.getBusinessName(), // 제조자
                application.getBusinessNumber(), // 사업자 등록 번호
                application.getOwnerName(), // 대표자명
                application.getBusinessName() + "/" + application.getManagerPhone(), // A/S 책임자/전화번호
                application.getEmail(), // 전자 우편 주소
                application.getBusinessAddress() + " " + application.getBusinessAddressDetail(), // 사업장 소재지
                application.getTelecomSalesNumber() // 통신판매업 신고 번호
        );
    }

    /**
     * 파일명으로 서류 타입 판단
     */
    private DocumentType determineDocumentType(String filename) {
        String lowerName = filename.toLowerCase();

        if (lowerName.contains("business") || lowerName.contains("사업자")) {
            return DocumentType.BUSINESS_LICENSE;
        } else if (lowerName.contains("telecom") || lowerName.contains("통신판매") || lowerName.contains("신고증")) {
            return DocumentType.TELECOM_CERTIFICATION;
        } else if (lowerName.contains("portfolio") || lowerName.contains("포트폴리오")) {
            return DocumentType.PORTFOLIO;
        }

        return DocumentType.OTHER;
    }

    /**
     * 필수 서류 검증 (사업자등록증, 통신판매업신고증)
     */
    private void validateRequiredDocuments(List<ArtistDocument> documents) {
        boolean hasBusinessLicense = documents.stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.BUSINESS_LICENSE);

        boolean hasTelecomCertification = documents.stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.TELECOM_CERTIFICATION);

        List<String> missingDocs = new ArrayList<>();
        if (!hasBusinessLicense) {
            missingDocs.add("사업자등록증");
        }
        if (!hasTelecomCertification) {
            missingDocs.add("통신판매업신고증");
        }

        if (!missingDocs.isEmpty()) {
            throw new ServiceException("400",
                    "필수 서류가 누락되었습니다: " + String.join(", ", missingDocs) +
                            ". 파일명에 '사업자' 또는 '통신판매'를 포함해주세요.");
        }
    }
}
