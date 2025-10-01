package com.back.domain.artist.service;

import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
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
    private final UserRepository userRepository;

    /**
     * 전체 신청서 목록 조회 (페이징)
     * TODO: 대시보드 파트와 겹치는 부분이어서 통합 고려
     */
    public Page<ArtistApplicationSimpleResponse> getAllApplications(Pageable pageable) {
        Page<ArtistApplication> applications =
                artistApplicationRepository.findAllByOrderByCreateDateDesc(pageable);

        return applications.map(ArtistApplicationSimpleResponse::from);
    }

    /**
     * 신청서 상세 조회
     * @param applicationId 신청서 ID
     * @return 신청서 상세 정보
     */
    public ArtistApplicationResponse getApplicationById(Long applicationId) {
        ArtistApplication application = artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ServiceException("404", "신청서를 찾을 수 없습니다."));

        return ArtistApplicationResponse.from(application);
    }

    /**
     * 신청서 승인
     * @param applicationId 신청서 ID
     * @param adminId 승인 처리하는 관리자 ID
     * @param adminName 승인 처리하는 관리자 이름
     */
    @Transactional
    public void approveApplication(Long applicationId, Long adminId, String adminName) {
        // 1. 신청서 조회
        ArtistApplication application = artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ServiceException("404", "신청서를 찾을 수 없습니다."));

        // 2. 신청서 승인
        application.approve(adminId, adminName);

        // 3. User Role을 ARTIST로 승급
        User user = application.getUser();
        user.becomeArtist();

    }

}
