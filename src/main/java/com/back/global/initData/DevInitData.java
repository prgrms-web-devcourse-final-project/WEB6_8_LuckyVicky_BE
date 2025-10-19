package com.back.global.initData;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.entity.*;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.product.tag.entity.Tag;
import com.back.domain.product.tag.repository.TagRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductRepository productRepository;
    private final org.springframework.context.ApplicationContext applicationContext;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.createUsers();
            self.createCategoriesAndTags();
            
            // 태그 생성 후 TagDictionary 강제 갱신
            try {
                log.info("===== TagDictionary 갱신 시작 =====");
                var tagDictionary = applicationContext.getBean(
                    com.back.domain.recommendation.service.TagDictionary.class
                );
                tagDictionary.refresh();
                log.info("===== TagDictionary 갱신 완료 =====");
            } catch (Exception e) {
                log.warn("TagDictionary 갱신 실패 (추천 기능 사용 불가): {}", e.getMessage());
            }
            
            self.createProducts();
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

    @Transactional
    public void createCategoriesAndTags() {
        log.info("===== 카테고리 및 태그 생성 시작 =====");

        // 카테고리 생성
        if (categoryRepository.count() == 0) {
            Category stationery = Category.builder()
                    .categoryName("문구")
                    .subCategories(new ArrayList<>())
                    .build();
            categoryRepository.save(stationery);

            Category design = Category.builder()
                    .categoryName("디자인소품")
                    .subCategories(new ArrayList<>())
                    .build();
            categoryRepository.save(design);

            log.info("카테고리 2개 생성 완료 (문구, 디자인소품)");
        }

        // 태그 생성
        if (tagRepository.count() == 0) {
            String[] tagNames = {
                "부드러운", "실용적인", "모던한", "빈티지", "미니멀",
                "화려한", "심플한", "귀여운", "세련된", "독특한",
                "클래식", "트렌디", "내추럴", "포멀", "캐주얼"
            };

            for (String tagName : tagNames) {
                Tag tag = Tag.builder()
                        .name(tagName)
                        .build();
                tagRepository.save(tag);
            }

            log.info("태그 {}개 생성 완료", tagNames.length);
        }

        log.info("===== 카테고리 및 태그 생성 완료 =====");
    }

    @Transactional
    public void createProducts() {
        log.info("===== 상품 더미 데이터 생성 시작 =====");

        if (productRepository.count() > 0) {
            log.info("상품 데이터가 이미 존재하여 스킵합니다.");
            return;
        }

        User artist = userRepository.findByEmail("artist1@artist.com")
                .orElseThrow(() -> new RuntimeException("아티스트 계정을 찾을 수 없습니다"));

        Category category = categoryRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다"));

        List<Tag> tags = tagRepository.findAll();
        if (tags.isEmpty()) {
            throw new RuntimeException("태그를 찾을 수 없습니다");
        }

        // 상품 데이터 (50개로 대폭 증가)
        String[][] productData = {
                // 필기구 (20개)
                {"부드러운 4B 연필", "베이직스튜디오", "15000", "10", "부드러운,실용적인,심플한"},
                {"빈티지 만년필", "클래식펜", "45000", "15", "빈티지,세련된,클래식"},
                {"모던 볼펜 세트", "어반라이프", "18000", "20", "모던한,실용적인,세련된"},
                {"내추럴 우드 펜", "포레스트", "22000", "10", "내추럴,심플한,클래식"},
                {"화려한 형광펜 세트", "컬러풀", "12000", "0", "화려한,실용적인,트렌디"},
                {"세련된 샤프펜슬", "엘레강스", "25000", "15", "세련된,모던한,포멀"},
                {"귀여운 젤펜 12색", "큐티팩토리", "16000", "10", "귀여운,화려한,캐주얼"},
                {"심플 블랙펜", "미니멀웍스", "8000", "0", "심플한,미니멀,실용적인"},
                {"독특한 디자인 만년필", "유니크", "38000", "20", "독특한,세련된,빈티지"},
                {"트렌디 컬러펜 세트", "모던펜", "14000", "10", "트렌디,화려한,모던한"},
                {"클래식 잉크펜", "레트로", "32000", "15", "클래식,빈티지,포멀"},
                {"부드러운 수성펜", "소프트터치", "11000", "5", "부드러운,심플한,실용적인"},
                {"실용적인 다용도펜", "올라운드", "9000", "0", "실용적인,심플한,캐주얼"},
                {"미니멀 화이트펜", "퓨어화이트", "13000", "10", "미니멀,심플한,모던한"},
                {"세련된 메탈펜", "메탈릭", "27000", "20", "세련된,모던한,포멀"},
                {"귀여운 캐릭터펜", "스위티", "7000", "0", "귀여운,화려한,캐주얼"},
                {"내추럴 대나무펜", "에코", "19000", "15", "내추럴,심플한,클래식"},
                {"화려한 글리터펜", "스파클", "10000", "10", "화려한,귀여운,트렌디"},
                {"모던 슬림펜", "슬림라인", "15000", "5", "모던한,세련된,미니멀"},
                {"빈티지 잉크펜 세트", "올드스쿨", "42000", "15", "빈티지,클래식,세련된"},
                
                // 노트/메모지 (15개)
                {"미니멀 노트", "심플노트", "8000", "0", "미니멀,심플한,모던한"},
                {"귀여운 메모지", "큐티팩토리", "9000", "10", "귀여운,화려한,캐주얼"},
                {"세련된 다이어리", "엘레강스북", "28000", "15", "세련된,포멀,클래식"},
                {"빈티지 노트북", "레트로페이퍼", "18000", "10", "빈티지,심플한,클래식"},
                {"모던 스케치북", "아트라인", "24000", "20", "모던한,실용적인,세련된"},
                {"화려한 스티커 노트", "컬러풀", "12000", "5", "화려한,귀여운,트렌디"},
                {"심플 메모패드", "베이직", "6000", "0", "심플한,미니멀,실용적인"},
                {"내추럴 크라프트 노트", "에코노트", "14000", "10", "내추럴,심플한,클래식"},
                {"트렌디 포켓노트", "스몰북", "7500", "5", "트렌디,실용적인,캐주얼"},
                {"독특한 디자인 다이어리", "유니크북", "32000", "15", "독특한,세련된,모던한"},
                {"부드러운 스케치북", "소프트페이퍼", "19000", "10", "부드러운,실용적인,심플한"},
                {"귀여운 캐릭터 노트", "스위티북", "11000", "0", "귀여운,화려한,캐주얼"},
                {"클래식 양장 다이어리", "프리미엄북", "45000", "20", "클래식,세련된,포멀"},
                {"미니멀 플래너", "심플플랜", "22000", "15", "미니멀,모던한,실용적인"},
                {"화려한 패턴 노트", "패턴팩토리", "13000", "10", "화려한,트렌디,귀여운"},
                
                // 기타 문구류 (15개)
                {"세련된 북마크", "엘레강스", "6000", "5", "세련된,미니멀,포멀"},
                {"트렌디 마스킹테이프", "테이프랩", "5000", "0", "트렌디,화려한,귀여운"},
                {"독특한 디자인 클립", "유니크", "7000", "5", "독특한,모던한,세련된"},
                {"모던 펜 케이스", "어반라이프", "18000", "15", "모던한,실용적인,세련된"},
                {"귀여운 지우개 세트", "큐티", "4500", "0", "귀여운,화려한,캐주얼"},
                {"심플 자 세트", "베이직룰러", "8500", "10", "심플한,실용적인,미니멀"},
                {"화려한 스티커 세트", "컬러풀", "12000", "20", "화려한,귀여운,트렌디"},
                {"내추럴 우드 클립", "에코클립", "9500", "5", "내추럴,심플한,클래식"},
                {"세련된 책갈피 세트", "북마크", "11000", "15", "세련된,모던한,포멀"},
                {"빈티지 스탬프", "레트로스탬프", "16000", "10", "빈티지,독특한,클래식"},
                {"미니멀 풀박스", "심플박스", "25000", "20", "미니멀,모던한,실용적인"},
                {"트렌디 데코 테이프", "데코랩", "6500", "5", "트렌디,화려한,귀여운"},
                {"부드러운 지우개", "소프트", "3500", "0", "부드러운,실용적인,심플한"},
                {"모던 클립보드", "보드", "14000", "10", "모던한,실용적인,세련된"},
                {"귀여운 포스트잇", "스티키노트", "5500", "5", "귀여운,화려한,실용적인"}
        };

        int createdCount = 0;
        for (String[] data : productData) {
            try {
                Product product = Product.builder()
                        .user(artist)
                        .category(category)
                        .name(data[0])
                        .brandName(data[1])
                        .price(Integer.parseInt(data[2]))
                        .discountRate(Integer.parseInt(data[3]))
                        .bundleShippingAvailable(true)
                        .deliveryCharge(0)
                        .additionalShippingCharge(3000)
                        .deliveryType(DeliveryType.FREE)
                        .conditionalFreeAmount(null)
                        .stock(100)
                        .description("고품질 " + data[0] + "입니다. 작가의 정성이 담긴 특별한 상품을 만나보세요.")
                        .sellingStatus(SellingStatus.SELLING)
                        .displayStatus(DisplayStatus.DISPLAYING)
                        .minQuantity(1)
                        .maxQuantity(10)
                        .productModelName(data[0])
                        .certification(false)
                        .origin("대한민국")
                        .material("종이/플라스틱/목재")
                        .size("표준")
                        .isPlanned(false)
                        .isRestock(false)
                        .sellingStartDate(LocalDateTime.now().minusDays(7))
                        .sellingEndDate(LocalDateTime.now().plusMonths(6))
                        .productTags(new HashSet<>())
                        .images(new ArrayList<>())
                        .isDeleted(false)
                        .build();

                // 태그 추가
                String[] productTagNames = data[4].split(",");
                for (String tagName : productTagNames) {
                    Tag tag = tags.stream()
                            .filter(t -> t.getName().equals(tagName.trim()))
                            .findFirst()
                            .orElse(null);

                    if (tag != null) {
                        ProductTagMapping productTag = ProductTagMapping.builder()
                                .product(product)
                                .tag(tag)
                                .build();
                        product.getProductTags().add(productTag);
                    }
                }

                productRepository.save(product);
                createdCount++;
                
                if (createdCount % 10 == 0) {
                    log.info("상품 생성 진행 중: {}/{}개", createdCount, productData.length);
                }
            } catch (Exception e) {
                log.error("상품 생성 실패: {} - {}", data[0], e.getMessage());
            }
        }

        log.info("===== 상품 더미 데이터 {}개 생성 완료 =====", createdCount);
        
        // 생성된 상품 통계
        log.info("총 상품 수: {}개", productRepository.count());
        log.info("전시중인 상품: {}개", productRepository.count());
    }
}
