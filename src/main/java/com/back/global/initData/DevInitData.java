package com.back.global.initData;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    @Autowired
    @Lazy
    private DevInitData self;

    private final UserRepository userRepository;
    private final ArtistApplicationRepository artistApplicationRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.createUsers();
        };
    }

    @Transactional
    public void createUsers() {
        log.info("===== 개발 환경 초기 데이터 생성 시작 =====");

        log.info("관리자 계정 생성 중...");
        createUser("admin@admin.com", "admin1234!", "관리자", "010-0000-0000", Role.ADMIN);
        log.info("관리자 계정 생성 완료: admin@admin.com");

        log.info("아티스트 계정 3개 생성 중...");
        for (int i = 1; i <= 3; i++) {
            String email = String.format("artist%d@artist.com", i);
            String name = String.format("아티스트%d", i);
            String phone = String.format("010-1%03d-%04d", i, 1000 + i);
            User artist = createUser(email, "1234qwer!", name, phone, Role.ARTIST);

            // 아티스트 프로필 생성
            createArtistProfile(artist, name);
        }
        log.info("아티스트 계정 3개 생성 완료");

        log.info("일반 사용자 계정 5개 생성 중...");
        for (int i = 1; i <= 5; i++) {
            String email = String.format("user%d@user.com", i);
            String name = String.format("유저%d", i);
            String phone = String.format("010-2%03d-%04d", i, 2000 + i);
            createUser(email, "1234qwer!", name, phone, Role.USER);
        }
        log.info("일반 사용자 계정 5개 생성 완료");

        log.info("===== 개발 환경 초기 데이터 생성 완료 =====");
        log.info("총 생성된 계정: 9개 (관리자 1개, 아티스트 3개, 일반 사용자 5개)");
    }

    private User createUser(String email, String password, String name, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            log.debug("계정 이미 존재 ({}): 스킵", email);
            return userRepository.findByEmail(email).orElseThrow();
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.createLocalUser(email, encodedPassword, name, phone);

        // 역할에 따라 승급
        switch (role) {
            case ARTIST -> user.becomeArtist();
            case ADMIN -> user.becomeAdmin();
            case USER -> {}
            default -> throw new IllegalArgumentException("지원하지 않는 Role: " + role);
        }

        User savedUser = userRepository.save(user);
        log.debug("{} 계정 생성: {}", role, email);

        return savedUser;
    }

    private void createArtistProfile(User artist, String artistName) {
        // 이미 프로필이 있으면 스킵
        if (artistProfileRepository.findByUserId(artist.getId()).isPresent()) {
            log.debug("아티스트 프로필 이미 존재 (userId: {}): 스킵", artist.getId());
            return;
        }

        // 더미 ArtistApplication 생성
        ArtistApplication application = ArtistApplication.builder()
                .user(artist)
                .ownerName(artist.getName())
                .email(artist.getEmail())
                .phone(artist.getPhone())
                .artistName(artistName)
                .businessNumber("123-45-67890")
                .businessName("테스트 사업자")
                .businessAddress("서울시 테스트구 테스트로 123")
                .businessAddressDetail("테스트빌딩 101호")
                .businessZipCode("12345")
                .telecomSalesNumber("2024-서울-0001")
                .snsAccount("@test_artist")
                .mainProducts("일러스트, 디지털 아트")
                .managerPhone(artist.getPhone())
                .bankName("테스트은행")
                .bankAccount("1234567890")
                .accountName(artist.getName())
                .build();

        ArtistApplication savedApplication = artistApplicationRepository.save(application);

        // ArtistProfile 생성
        ArtistProfile profile = ArtistProfile.fromApplication(artist, savedApplication);
        artistProfileRepository.save(profile);

        log.debug("아티스트 프로필 생성 완료: {}", artistName);
    }
}