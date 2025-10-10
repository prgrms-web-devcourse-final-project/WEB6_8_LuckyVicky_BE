package com.back.global.initData;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProdInitData {

    @Autowired
    @Lazy
    private ProdInitData self;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArtistApplicationRepository artistApplicationRepository;

    @Value("${INIT_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${INIT_ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${INIT_ARTIST_EMAIL}")
    private String artistEmail;

    @Value("${INIT_ARTIST_PASSWORD}")
    private String artistPassword;

    @Bean
    ApplicationRunner prodInitDataApplicationRunner() {
        return args -> {
            self.createInitialData();
        };
    }

    @Transactional
    public void createInitialData() {
        log.info("========================================");
        log.info("===== 프로덕션 환경 초기 데이터 생성 시작 =====");
        log.info("========================================");

        // 1. 관리자 계정 생성
        createAdminAccount();

        // 2. 작가 계정 생성
        createArtistAccount();

        // 3. 일반 사용자 계정 생성
        createUserAccount();

        log.info("========================================");
        log.info("===== 프로덕션 환경 초기 데이터 생성 완료 =====");
        log.info("총 생성된 계정: 3개 (관리자 1개, 작가 1개, 일반 사용자 1개)");
        log.info("========================================");
    }

    /**
     * 관리자 계정 생성
     */
    private void createAdminAccount() {
        log.info(">>> 관리자 계정 생성 중...");

        String name = "시스템관리자";
        String phone = "010-0000-0000";

        if (userRepository.existsByEmail(adminEmail)) {
            log.warn("관리자 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        String encodedPassword = passwordEncoder.encode(adminPassword);
        User admin = User.createLocalUser(adminEmail, encodedPassword, name, phone);
        admin.becomeAdmin();

        userRepository.save(admin);
        log.info("✓ 관리자 계정 생성 완료: {} ({})", name, adminEmail);
    }

    /**
     * 작가 계정 생성 (신청서 포함)
     */
    private void createArtistAccount() {
        log.info(">>> 작가 계정 생성 중...");

        String name = "대표작가";
        String phone = "010-1111-1111";

        // 이미 계정이 존재하는지 확인
        if (userRepository.existsByEmail(artistEmail)) {
            log.warn("작가 계정이 이미 존재합니다: {}", artistEmail);
            return;
        }

        // 1. User 생성
        String encodedPassword = passwordEncoder.encode(artistPassword);
        User artist = User.createLocalUser(artistEmail, encodedPassword, name, phone);
        userRepository.save(artist);
        log.info("✓ 작가 유저 생성 완료: {} ({})", name, artistEmail);

        // 2. ArtistApplication 생성 (신청서)
        ArtistApplication application = ArtistApplication.builder()
                .user(artist)
                .ownerName(name)
                .email(artistEmail)
                .phone(phone)
                .artistName(name)
                .businessNumber("123-45-67890")
                .businessName("모리모리 아트 스튜디오")
                .businessAddress("서울특별시 강남구 테헤란로 123")
                .businessAddressDetail("모리빌딩 5층")
                .businessZipCode("06234")
                .telecomSalesNumber("2024-서울강남-12345")
                .snsAccount("@mori_mori_artist")
                .mainProducts("회화, 조각 작품")
                .managerPhone(phone)
                .bankName("국민은행")
                .bankAccount("123456-78-901234")
                .accountName(name)
                .build();

        artistApplicationRepository.save(application);
        log.info("✓ 작가 신청서 생성 완료 (PENDING 상태)");

        // 3. 신청서 자동 승인 및 작가 권한 부여
        application.approve(1L, "시스템");
        artist.becomeArtist();

        userRepository.save(artist);
        artistApplicationRepository.save(application);
        log.info("✓ 작가 신청서 승인 완료 및 작가 권한 부여 완료");
    }

    /**
     * 일반 사용자 계정 생성
     */
    private void createUserAccount() {
        log.info(">>> 일반 사용자 계정 생성 중...");

        String email = "user@user.com";
        String password = "user1234!";
        String name = "일반사용자";
        String phone = "010-2222-2222";

        // 이미 계정이 존재하는지 확인
        if (userRepository.existsByEmail(email)) {
            log.warn("일반 사용자 계정이 이미 존재합니다: {}", email);
            return;
        }

        // User 생성
        String encodedPassword = passwordEncoder.encode(password);
        User user = User.createLocalUser(email, encodedPassword, name, phone);

        userRepository.save(user);
        log.info("✓ 일반 사용자 계정 생성 완료: {} ({})", name, email);
    }

}
