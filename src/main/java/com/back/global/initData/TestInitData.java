package com.back.global.initData;

import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.service.AuthService;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.tag.entity.Tag;
import com.back.domain.product.tag.repository.TagRepository;
import com.back.domain.user.entity.Provider;
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
    private final TagRepository tagRepository;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
            self.work3(); // 카테고리 생성
            self.work4(); // 태그 생성
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
        createOAuthUser("oauth@user.com", "OAuth테스트유저", Provider.GOOGLE, "google-test-id-123");
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
        // 0) 작성자 조회
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();

        Category category = categoryRepository.findById((1L))
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().categoryName("테스트 카테고리").build()
                ));

        LocalDateTime now = LocalDateTime.now();

        // 1. 펀딩 1
        Funding f0 = Funding.create(
                user1,
                "펀딩 1 입니다.",
                "펀딩 1이요~~",
                category,
                "www.example.com",
                1_000_000L,
                10000,
                50,
                now.plusDays(5),
                now.plusDays(25),
                FundingStatus.OPEN
        );
        fundingRepository.save(f0);
        log.info("테스트 펀딩 저장 완료 id={}", f0.getId());

        // 2. 신규 앨범 제작
        Funding f1 = Funding.create(
                user1,
                "신규 앨범 제작",
                "새로운 앨범을 만듭니다",
                category,
                "image1.jpg",
                1_000_000L,
                15000,
                100,
                now.plusDays(5),
                now.plusDays(25),
                FundingStatus.OPEN
        );
        f1.increaseParticipantCount(50);
        fundingRepository.save(f1);

        // 3. 한정판 포토북
        Funding f2 = Funding.create(
                user1,
                "한정판 포토북",
                "프리미엄 포토북",
                category,
                "image2.jpg",
                500_000L,
                35000,
                50,
                now.plusDays(3),
                now.plusDays(5),
                FundingStatus.OPEN
        );
        f2.increaseParticipantCount(30);
        fundingRepository.save(f2);

        // 4. 프리미엄 굿즈
        Funding f3 = Funding.create(
                user1,
                "프리미엄 굿즈",
                "한정판 굿즈",
                category,
                "image3.jpg",
                2_000_000L,
                80000,
                30,
                now.plusDays(1),
                now.plusDays(40),
                FundingStatus.PENDING
        );
        fundingRepository.save(f3);
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

    @Transactional
    public void work4() {
        tagRepository.save(Tag.builder().name("태그1").build());
        tagRepository.save(Tag.builder().name("태그2").build());
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

    // OAuth 테스트 사용자 생성
    private void createOAuthUser(String email, String name, Provider provider, String providerId) {
        if (userRepository.existsByEmail(email)) {
            log.debug("OAuth 사용자 이미 존재: {}", email);
            return;
        }

        User oauthUser = User.createOAuthUser(email, name, provider, providerId);
        userRepository.save(oauthUser);
        log.info("OAuth 테스트 사용자 생성: email={}, provider={}", email, provider);
    }
}