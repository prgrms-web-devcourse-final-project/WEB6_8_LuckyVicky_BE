package com.back.global.initData;

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
            createUser(email, "1234qwer!", name, phone, Role.ARTIST);
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

    private void createUser(String email, String password, String name, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            log.debug("계정 이미 존재 ({}): 스킵", email);
            return;
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

        userRepository.save(user);
        log.debug("{} 계정 생성: {}", role, email);
    }
}