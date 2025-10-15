package com.back.domain.artist.service;

import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자용 작가 신청 심사 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistApplicationAdminService {

    private final ArtistApplicationRepository artistApplicationRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final UserRepository userRepository;

    /**
     * 전체 신청서 목록 조회 (페이징)
     * TODO: 대시보드 파트와 겹치는 부분이어서 통합 고려
     */
    public Page<ArtistApplicationSimpleResponse> getAllApplications(Pageable pageable) {
        // 새로운 동적 쿼리 사용 (keyword, status null로 전체 조회, 기본 정렬: submittedAt DESC)
        Page<ArtistApplication> applications =
                artistApplicationRepository.findArtistApplicationsForAdmin(
                        null,  // keyword: 검색어 없음 (전체 조회)
                        null,  // status: 상태 필터 없음 (전체 조회)
                        "submittedAt",  // 신청일자 기준 정렬
                        "DESC",  // 최신순
                        pageable
                );

        return applications.map(ArtistApplicationSimpleResponse::from);
    }

    /**
     * 신청서 상세 조회
     */
    public ArtistApplicationResponse getApplicationById(Long applicationId) {
        ArtistApplication application = getApplication(applicationId);

        return ArtistApplicationResponse.from(application);
    }

    /**
     * 신청서 승인
     * - User Role을 ARTIST로 변경
     * - ArtistProfile 생성
     * - ArtistApplication 상태를 APPROVED로 변경
     */
    @Transactional
    public void approveApplication(Long applicationId, Long adminId, String adminName) {
        // 1. 신청서 조회
        ArtistApplication application = getApplication(applicationId);

        // 2. 중복 승인 방지
        User user = application.getUser();
        validateNotAlreadyApproved(user.getId());

        // 3. User Role 변경 및 ArtistProfile 생성
        promoteToArtist(user, application);

        // 4. 신청서 상태 변경
        application.approve(adminId, adminName);

        log.info("작가 신청 승인 완료 - userId: {}, applicationId: {}, adminId: {}",
                user.getId(), applicationId, adminId);
    }

    /**
     * 신청서 거절
     * - ArtistApplication 상태를 REJECTED로 변경
     * - 거절 사유 저장
     */
    @Transactional
    public void rejectApplication(Long applicationId, Long adminId, String adminName, String rejectionReason) {
        // 1. 신청서 조회
        ArtistApplication application = getApplication(applicationId);

        // 2. 거절 사유 검증
        validateRejectionReason(rejectionReason);

        // 3. 신청서 거절 처리
        application.reject(adminId, adminName, rejectionReason);

        log.info("작가 신청 거절 완료 - userId: {}, applicationId: {}, adminId: {}, reason: {}",
                application.getUser().getId(), applicationId, adminId, rejectionReason);
    }


    // ===== 헬퍼 메서드 ===== ///

    /**
     * 신청서 조회
     */
    private ArtistApplication getApplication(Long applicationId) {
        return artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ServiceException("404", "신청서를 찾을 수 없습니다."));
    }

    /**
     * 중복 승인 방지 검증
     */
    private void validateNotAlreadyApproved(Long userId) {
        if (artistProfileRepository.existsByUserId(userId)) {
            throw new ServiceException("400", "이미 작가 프로필이 존재합니다.");
        }
    }

    /**
     * 거절 사유 검증
     */
    private void validateRejectionReason(String rejectionReason) {
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new ServiceException("400", "거절 사유는 필수입니다.");
        }
    }

    /**
     * 작가로 승급 처리
     * - User Role 변경
     * - ArtistProfile 생성
     */
    private void promoteToArtist(User user, ArtistApplication application) {
        // User Role 변경
        user.becomeArtist();

        // ArtistProfile 생성 및 저장
        ArtistProfile profile = ArtistProfile.fromApplication(user, application);
        artistProfileRepository.save(profile);
    }
}
