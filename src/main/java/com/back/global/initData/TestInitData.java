package com.back.global.initData;

import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.service.AuthService;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingOption;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
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

import java.time.LocalDateTime;

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
    private final FundingRepository fundingRepository;
    @Autowired
    private DevUserService devUserService;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
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

    @Transactional
    public void work2() {
        // 펀딩 생성
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();

        Funding funding = Funding.builder()
                .user(user1)
                .title("펀딩 1 입니다.")
                .description("펀딩 1이요~~")
                .imageUrl("www.example.com")
                .targetAmount(1000000)
                .startDate(LocalDateTime.of(2025, 9, 24, 12, 30, 0))
                .endDate(LocalDateTime.of(2025, 9, 29, 12, 30, 0))
                .status(FundingStatus.OPEN)
                .build();

        funding.attachOption(FundingOption.create("1 옵션이요~", 10000, 11110, 1));
        funding.attachOption(FundingOption.create("2 옵션이요~", 15000, 11110, 2));

        fundingRepository.save(funding);
        log.info("테스트 펀딩 저장 완료 id={}", funding.getId());
    }
}
