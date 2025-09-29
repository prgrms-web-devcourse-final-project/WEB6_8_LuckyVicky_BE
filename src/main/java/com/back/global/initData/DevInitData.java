package com.back.global.initData;

import com.back.domain.auth.service.AuthService;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.user.user.DevUserService;
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
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private DevUserService devUserService;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.createUsers();
        };
    }

    // TODO: 작가 신청/승인 API 완성 후 삭제
    // TODO: 삭제 후, 관리자 초기 계정 1개 세팅만 필요
    @Transactional
    public void createUsers() {
        log.info("===== 개발 환경 초기 데이터 생성 시작 =====");

        // 1. 관리자 계정 1개 생성
        log.info("관리자 계정 생성 중...");
        createUser("admin@admin.com", "admin1234!", "관리자", "010-0000-0000", Role.ADMIN);
        log.info("관리자 계정 생성 완료: admin@admin.com");

        // 2. ARTIST 계정 3개 생성
        log.info("아티스트 계정 3개 생성 중...");
        for (int i = 1; i <= 3; i++) {
            String email = String.format("artist%d@artist.com", i);
            String name = String.format("아티스트%d", i);
            String phone = String.format("010-1%03d-%04d", i, 1000 + i);

            createUser(email, "1234qwer!", name, phone, Role.ARTIST);
        }
        log.info("아티스트 계정 3개 생성 완료");

        // 3. USER 계정 5개 생성
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

    private void createUser(String email, String password, String name, String phone, Role role) {
        // 이미 존재하는 계정은 스킵
        if (userRepository.existsByEmail(email)) {
            log.debug("계정 이미 존재 ({}): 스킵", email);
            return;
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // User 엔티티 생성 (기본 USER 권한으로 생성)
        User user = User.createLocalUser(email, encodedPassword, name, phone);

        // 역할이 USER가 아니면 변경
        if (!Role.USER.equals(role)) {
            user.changeRole(role);
        }

        // 저장
        userRepository.save(user);
    }
}