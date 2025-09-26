package com.back.global.initData;

import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.service.AuthService;
import com.back.domain.user.entity.Role;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.user.user.DevUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test") // test 환경에서만 실행
@RequiredArgsConstructor
public class TestInitData {

    @Autowired
    @Lazy
    private TestInitData self;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
//            self.work2();
        };
    }

    @Transactional
    public void work1() {
        safeSignup("user1@user.com", "1234qwer!", "1234qwer!", "유저1", "010-1234-5678");
        safeSignup("user2@user.com", "1234qwer!", "1234qwer!", "유저2", "010-2345-6789");
        safeSignup("user3@user.com", "1234qwer!", "1234qwer!", "유저3", "010-3456-7890");

        devUserService.changeUserRoleByEmail("user1@user.com", Role.ARTIST);
    }

    private void safeSignup(String email, String password, String passwordConfirm ,String name, String phone) {
        try {
            SignUpRequest req = new SignUpRequest(
                    email,
                    password,
                    passwordConfirm,
                    name,
                    phone,
                    true,
                    true
            );

            authService.signUp(req);

        } catch (IllegalArgumentException e) {
            log.debug("Skip creating member ({}): {}", email, e.getMessage());
        }
    }

    @Autowired
    private DevUserService devUserService;
}

