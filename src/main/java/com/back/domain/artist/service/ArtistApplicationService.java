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
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.S3FileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 사용자용 작가 신청 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistApplicationService {

    private final ArtistApplicationRepository artistApplicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 작가 신청서 생성
     */
    @Transactional
    public Long createApplication(Long userId, ArtistApplicationRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));

        // 2. 중복 신청 검증 (심사 대기 중인 신청서가 있는지)
        validateDuplicateApplication(userId);

        // 3. 필수 서류 검증
        if (!request.hasRequiredDocuments()) {
            throw new ServiceException("400", "필수 서류가 누락되었습니다." + request.getMissingRequiredDocuments());
        }

        // 4. 작가 신청서 엔티티 생성 및 저장
        ArtistApplication savedApplication = createAndSaveApplication(user, request);

        // 5. 서류 정보 저장 (ArtistDocument 엔티티 생성)
        List<ArtistDocument> documents = createDocuments(savedApplication, request.documents());
        savedApplication.getDocuments().addAll(documents);

        log.info("작가 신청서 생성 완료: userId={}, applicationId={}", userId, savedApplication.getId());

        // 6. 알림 발송 - 모든 관리자에게 작가 인증 신청 알림
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
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.");
        }

        List<ArtistApplication> applications = artistApplicationRepository.findByUserIdOrderByCreateDateDesc(userId);

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

        // 본인 신청서인지 확인
        if (!application.getUser().getId().equals(userId)) {
            throw new ServiceException("403", "본인의 신청서만 조회할 수 있습니다.");
        }

        return ArtistApplicationResponse.from(application);
    }

    /**
     * 작가 신청 취소 (삭제)
     */
    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        // 1. 신청서 조회
        ArtistApplication application = getApplicationIfOwner(userId, applicationId);

        // 2. 취소 가능한 상태인지 확인
        validateCancellable(application);

        // 3. 신청서 삭제
        artistApplicationRepository.delete(application);

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
}
