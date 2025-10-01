package com.back.domain.dashboard.admin.controller;

import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.entity.DeliveryType;
import com.back.domain.product.product.entity.DisplayStatus;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminDashboardController 통합 테스트
 * 실제 DB 연동을 통한 End-to-End 테스트
 * 2025.10.01 실제 DB 연동으로 수정
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("관리자 대시보드 컨트롤러 통합 테스트")
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private ArtistApplicationRepository artistApplicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String BEARER_TOKEN = "Bearer test-admin-token-123";
    private static final String ADMIN_ROLE = "SUPER_ADMIN";

    private User artistUser;
    private User customerUser;
    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        // TestInitData에서 이미 생성된 사용자들 조회
        artistUser = userRepository.findByEmail("user1@user.com").orElseThrow();
        customerUser = userRepository.findByEmail("user2@user.com").orElseThrow();

        // TestInitData에서 생성된 카테고리 조회 (없으면 생성)
        defaultCategory = categoryRepository.findAll().stream()
                .filter(c -> "회화".equals(c.getCategoryName()))
                .findFirst()
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .categoryName("회화")
                            .parent(null)
                            .build();
                    return categoryRepository.save(category);
                });
    }

    @Test
    @DisplayName("관리자 대시보드 전체 현황 조회 성공 - 실제 DB 데이터 (일부 더미 포함)")
    void getOverview_Success_WithRealData() throws Exception {
        // Given - DB에 이미 TestInitData로 데이터가 있음
        long expectedUserCount = userRepository.count();
        long expectedFundingCount = fundingRepository.count();

        // When & Then - 실제 DB 연동된 부분만 검증
        MvcResult result = mockMvc.perform(get("/api/dashboard/admin/overview")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("range", "1M")
                        .param("granularity", "DAY")
                        .param("period", "MONTH")
                        .param("timezone", "Asia/Seoul"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("관리자 메인 현황 조회 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.overview").exists())
                .andExpect(jsonPath("$.data.overview.userCount.count").value(expectedUserCount))
                .andExpect(jsonPath("$.data.overview.fundingCount.count").value(expectedFundingCount))
                // Order 관련은 더미 (0)
                .andExpect(jsonPath("$.data.overview.orderStats.count").value(0))
                .andExpect(jsonPath("$.data.overview.salesStats.count").value(0))
                .andExpect(jsonPath("$.data.alerts").exists())
                .andReturn();

        // 추가 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("overview", "charts", "alerts");
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 성공 - 실제 DB 데이터")
    void getProducts_Success_WithRealData() throws Exception {
        // Given - 테스트 상품 생성
        Product product = createTestProduct(artistUser, "테스트 상품", SellingStatus.SELLING);
        productRepository.save(product);

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/products")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "registeredAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("관리자 상품 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.content[0].productId").exists())
                .andExpect(jsonPath("$.data.content[0].name").exists())
                .andExpect(jsonPath("$.data.content[0].artist.name").exists());
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 - 판매 상태 필터")
    void getProducts_WithSellingStatus() throws Exception {
        // Given
        Product sellingProduct = createTestProduct(artistUser, "판매중 상품", SellingStatus.SELLING);
        Product endProduct = createTestProduct(artistUser, "판매종료 상품", SellingStatus.END_OF_SALE);
        productRepository.save(sellingProduct);
        productRepository.save(endProduct);

        // When & Then - 판매중만 조회
        mockMvc.perform(get("/api/dashboard/admin/products")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sellingStatus", "SELLING")
                        .param("sort", "registeredAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 - 판매상태 필터 (판매종료)")
    void getProducts_WithEndOfSaleStatus() throws Exception {
        // Given
        Product endProduct = createTestProduct(artistUser, "판매종료 상품", SellingStatus.END_OF_SALE);
        productRepository.save(endProduct);

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/products")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sellingStatus", "END_OF_SALE")
                        .param("sort", "registeredAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 - 키워드 검색")
    void getProducts_WithKeyword() throws Exception {
        // Given
        Product product = createTestProduct(artistUser, "특별한 상품", SellingStatus.SELLING);
        productRepository.save(product);

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/products")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("keyword", "특별한")
                        .param("sort", "registeredAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("관리자 사용자 목록 조회 성공 - 실제 DB 데이터")
    void getUsers_Success_WithRealData() throws Exception {
        // Given - TestInitData에 이미 사용자들이 있음
        long expectedUserCount = userRepository.count();

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/users")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "joinedAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("관리자 사용자 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(expectedUserCount))
                .andExpect(jsonPath("$.data.content[0].userId").exists())
                .andExpect(jsonPath("$.data.content[0].nickname").exists());
    }

    @Test
    @DisplayName("관리자 사용자 목록 조회 - 역할 필터 (작가만)")
    void getUsers_WithRoleFilter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/users")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("role", "ARTIST")
                        .param("sort", "joinedAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("관리자 펀딩 목록 조회 성공 - 실제 DB 데이터")
    void getFundings_Success_WithRealData() throws Exception {
        // Given - TestInitData에 이미 펀딩이 있음
        long expectedFundingCount = fundingRepository.count();

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/fundings")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "endDate")
                        .param("order", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("관리자 펀딩 모니터링 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(expectedFundingCount))
                .andExpect(jsonPath("$.data.content[0].fundingId").exists())
                .andExpect(jsonPath("$.data.content[0].title").exists())
                .andExpect(jsonPath("$.data.content[0].achievementRate").isNumber());
    }

    @Test
    @DisplayName("관리자 펀딩 목록 조회 - 상태 필터")
    void getFundings_WithStatusFilter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/fundings")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "OPEN")
                        .param("sort", "endDate")
                        .param("order", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("관리자 입점 신청 목록 조회 성공 - 실제 DB 데이터")
    void getArtistApplications_Success_WithRealData() throws Exception {
        // Given - 테스트 입점 신청 생성
        ArtistApplication application = createTestApplication(customerUser, "테스트 작가", ApplicationStatus.PENDING);
        artistApplicationRepository.save(application);

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/artist-applications")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "submittedAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("입점 신청 목록 조회 성공"))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].applicationId").exists())
                .andExpect(jsonPath("$.data.content[0].status").exists());
    }

    @Test
    @DisplayName("관리자 입점 신청 목록 조회 - 상태 필터 (대기중만)")
    void getArtistApplications_WithPendingStatus() throws Exception {
        // Given
        ArtistApplication pending = createTestApplication(customerUser, "대기중 작가", ApplicationStatus.PENDING);
        artistApplicationRepository.save(pending);

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/artist-applications")
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE)
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "PENDING")
                        .param("sort", "submittedAt")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("관리자 입점 신청 상세 조회 성공 - 실제 DB 데이터")
    void getArtistApplicationDetail_Success_WithRealData() throws Exception {
        // Given
        ArtistApplication application = createTestApplication(customerUser, "상세조회 테스트 작가", ApplicationStatus.PENDING);
        ArtistApplication saved = artistApplicationRepository.save(application);

        // When & Then
        mockMvc.perform(get("/api/dashboard/admin/artist-applications/{applicationId}", saved.getId())
                        .header("Authorization", BEARER_TOKEN)
                        .header("X-Admin-Role", ADMIN_ROLE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("입점 신청 상세 조회 성공"))
                .andExpect(jsonPath("$.data.applicationId").value(saved.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.artist").exists())
                .andExpect(jsonPath("$.data.contact").exists())
                .andExpect(jsonPath("$.data.business").exists());
    }

    // ===== 헬퍼 메서드 =====

    /**
     * 테스트용 상품 생성
     */
    private Product createTestProduct(User artist, String name, SellingStatus status) {
        return Product.builder()
                .name(name)
                .brandName("테스트 브랜드")
                .user(artist)
                .category(defaultCategory) // TestInitData에서 생성된 카테고리 사용
                .sellingStatus(status)
                .displayStatus(DisplayStatus.DISPLAYING)
                .price(10000)
                .discountRate(0)
                .stock(100)
                .description("테스트 상품 설명")
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(3000)
                .deliveryType(DeliveryType.FREE)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("테스트 모델")
                .certification(false)
                .origin("대한민국")
                .material("종이")
                .size("A4")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
    }

    /**
     * 테스트용 입점 신청 생성 (항상 PENDING 상태)
     */
    private ArtistApplication createTestApplication(User user, String artistName, ApplicationStatus ignoredStatus) {
        return ArtistApplication.builder()
                .user(user)
                .ownerName("대표자명")
                .artistName(artistName)
                .email("test@test.com")
                .phone("010-1234-5678")
                .businessNumber("123-45-67890")
                .businessAddress("서울특별시 강남구")
                .businessAddressDetail("123동 456호")
                .mainProducts("회화,조각")
                .build();
        // status는 빌더에 없고 기본값 PENDING으로 설정됨
    }
}
