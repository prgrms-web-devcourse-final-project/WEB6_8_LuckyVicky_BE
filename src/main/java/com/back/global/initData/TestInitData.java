package com.back.global.initData;

import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.service.AuthService;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingOption;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
            self.work3(); // 카테고리 생성
        };
    }

    @Transactional
    public void work1() {
        safeSignup("user1@user.com", "1234qwer!", "1234qwer!", "유저1", "010-1234-5678");
        safeSignup("user2@user.com", "1234qwer!", "1234qwer!", "유저2", "010-2345-6789");
        safeSignup("user3@user.com", "1234qwer!", "1234qwer!", "유저3", "010-3456-7890");

        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();
        user1.becomeArtist();

        createUser("artist1@artist.com", "1234qwer!", "작가1", "010-2111-1111", Role.ARTIST);
        createUser("admin1@admin.com", "1234qwer!", "관리자1", "010-3111-1111", Role.ADMIN);
    }

    private void safeSignup(String email, String password, String passwordConfirm, String name, String phone) {
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

    private void createUser(String email, String password, String name, String phone, Role role) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = User.createLocalUser(email, encodedPassword, name, phone);

        // 역할에 따라 승급
        switch (role) {
            case ARTIST -> user.becomeArtist();
            case ADMIN -> user.becomeAdmin();
            case USER -> {
            } // 기본값
            default -> throw new IllegalArgumentException("지원하지 않는 Role: " + role);
        }

        userRepository.save(user);
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

        Funding funding1 = Funding.builder()
                .user(user1)
                .title("신규 앨범 제작")
                .description("새로운 앨범을 만듭니다")
                .imageUrl("image1.jpg")
                .targetAmount(1000000L)
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(25))
                .status(FundingStatus.OPEN)
                .build();

        funding1.attachOption(FundingOption.create("일반 앨범", 15000L, 100, 1));
        funding1.increaseParticipantCount(50);
        fundingRepository.save(funding1);

        // 2. 포토북 펀딩
        Funding funding2 = Funding.builder()
                .user(user1)
                .title("한정판 포토북")
                .description("프리미엄 포토북")
                .imageUrl("image2.jpg")
                .targetAmount(500000L)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(5))
                .status(FundingStatus.OPEN)
                .build();

        funding2.attachOption(FundingOption.create("포토북 세트", 35000L, 50, 1));
        funding2.increaseParticipantCount(30);
        fundingRepository.save(funding2);

        // 3. 고가 굿즈
        Funding funding3 = Funding.builder()
                .user(user1)
                .title("프리미엄 굿즈")
                .description("한정판 굿즈")
                .imageUrl("image3.jpg")
                .targetAmount(2000000L)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(40))
                .status(FundingStatus.OPEN)
                .build();

        funding3.attachOption(FundingOption.create("프리미엄 세트", 80000L, 30, 1));
        funding3.increaseParticipantCount(10);
        fundingRepository.save(funding3);
    }

    @Transactional
    public void work3() {
        // 카테고리 생성
        log.info("테스트 카테고리 생성 시작");

        // 상위 카테고리 생성
        Category art = createCategoryIfNotExists("회화");
        Category sculpture = createCategoryIfNotExists("조각");
        Category craft = createCategoryIfNotExists("공예");
        Category design = createCategoryIfNotExists("디자인");
        Category photography = createCategoryIfNotExists("사진");

        // 하위 카테고리 생성 (회화)
        createSubCategoryIfNotExists("유화", art);
        createSubCategoryIfNotExists("수채화", art);
        createSubCategoryIfNotExists("아크릴화", art);

        // 하위 카테고리 생성 (조각)
        createSubCategoryIfNotExists("목조각", sculpture);
        createSubCategoryIfNotExists("석조각", sculpture);
        createSubCategoryIfNotExists("금속조각", sculpture);

        // 하위 카테고리 생성 (공예)
        createSubCategoryIfNotExists("도자기", craft);
        createSubCategoryIfNotExists("유리공예", craft);
        createSubCategoryIfNotExists("섬유공예", craft);

        // 하위 카테고리 생성 (디자인)
        createSubCategoryIfNotExists("제품디자인", design);
        createSubCategoryIfNotExists("그래픽디자인", design);
        createSubCategoryIfNotExists("패션디자인", design);

        // 하위 카테고리 생성 (사진)
        createSubCategoryIfNotExists("풍경사진", photography);
        createSubCategoryIfNotExists("인물사진", photography);
        createSubCategoryIfNotExists("예술사진", photography);

        log.info("테스트 카테고리 생성 완료");
    }

    /**
     * 카테고리가 없으면 생성
     */
    private Category createCategoryIfNotExists(String categoryName) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryName().equals(categoryName))
                .findFirst()
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .categoryName(categoryName)
                            .parent(null)
                            .build();
                    Category saved = categoryRepository.save(category);
                    log.info("카테고리 생성: {}", categoryName);
                    return saved;
                });
    }

    /**
     * 하위 카테고리가 없으면 생성
     */
    private Category createSubCategoryIfNotExists(String categoryName, Category parent) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryName().equals(categoryName) &&
                        c.getParent() != null &&
                        c.getParent().getId().equals(parent.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .categoryName(categoryName)
                            .parent(parent)
                            .build();
                    Category saved = categoryRepository.save(category);
                    log.info("하위 카테고리 생성: {} (부모: {})", categoryName, parent.getCategoryName());
                    return saved;
                });
    }
}
