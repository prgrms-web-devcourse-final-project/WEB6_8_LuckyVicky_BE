package com.back.domain.dashboard.customer.service;


import com.back.domain.dashboard.customer.dto.response.FundingResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingContribution;
import com.back.domain.funding.entity.FundingOption;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * DashboardServiceImpl 테스트
 * 핵심 비즈니스 로직과 데이터 일관성에 집중
 * 2025.09.30 수정 - Funding 실제 DB 연동 테스트 추가
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("대시보드 서비스 구현체 테스트")
class DashboardServiceImplTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private FundingContributionRepository fundingContributionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testBuyer;
    private User testArtist;
    private String testAuthorization;
    private Funding activeFunding;
    private Funding endedFunding;

    @BeforeEach
    void setUp() {
        // 테스트 구매자 생성
        testBuyer = User.createLocalUser(
                "test-buyer@example.com",
                "password",
                "테스트구매자",
                "01012345678"
        );
        testBuyer = userRepository.save(testBuyer);

        // 테스트 작가 생성
        testArtist = User.createLocalUser(
                "test-artist@example.com",
                "password",
                "테스트작가",
                "01087654321"
        );
        testArtist.becomeArtist();  // 작가 역할로 변경
        testArtist = userRepository.save(testArtist);

        // JWT 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                testBuyer.getId(),
                testBuyer.getEmail(),
                testBuyer.getAvailableLoginRoles().get(0)
        );
        testAuthorization = "Bearer " + token;

        // FundingOption 먼저 생성
        FundingOption activeOption = FundingOption.builder()
                .name("테스트 리워드 A")
                .price(25000L)
                .stock(100)
                .sortOrder(1)
                .build();

        FundingOption endedOption = FundingOption.builder()
                .name("테스트 리워드 B")
                .price(30000L)
                .stock(50)
                .sortOrder(1)
                .build();

        // 진행중인 펀딩 생성 (옵션 포함)
        activeFunding = Funding.builder()
                .user(testArtist)
                .title("진행중인 펀딩")
                .description("테스트 펀딩입니다")
                .imageUrl("https://example.com/image1.jpg")
                .targetAmount(1000000L)
                .collectedAmount(500000L)
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(25))
                .status(FundingStatus.OPEN)
                .participantCount(5)
                .build();
        activeFunding.attachOption(activeOption);  // cascade로 함께 저장됨
        activeFunding = fundingRepository.save(activeFunding);

        // 종료된 펀딩 생성 (옵션 포함)
        endedFunding = Funding.builder()
                .user(testArtist)
                .title("종료된 펀딩")
                .description("테스트 펀딩입니다")
                .imageUrl("https://example.com/image2.jpg")
                .targetAmount(500000L)
                .collectedAmount(600000L)
                .startDate(LocalDateTime.now().minusDays(35))
                .endDate(LocalDateTime.now().minusDays(5))
                .status(FundingStatus.SUCCESS)
                .participantCount(10)
                .build();
        endedFunding.attachOption(endedOption);  // cascade로 함께 저장됨
        endedFunding = fundingRepository.save(endedFunding);

        // 저장된 옵션 가져오기 (cascade로 저장됨)
        activeOption = activeFunding.getOptions().get(0);
        endedOption = endedFunding.getOptions().get(0);

        // 펀딩 참여 내역 생성 (진행중)
        FundingContribution activeContribution = FundingContribution.builder()
                .funding(activeFunding)
                .option(activeOption)
                .buyer(testBuyer)
                .quantity(2)
                .unitPrice(25000L)
                .totalAmount(50000L)
                .paidAt(LocalDateTime.now().minusDays(3))
                .build();
        fundingContributionRepository.save(activeContribution);

        // 펀딩 참여 내역 생성 (종료)
        FundingContribution endedContribution = FundingContribution.builder()
                .funding(endedFunding)
                .option(endedOption)
                .buyer(testBuyer)
                .quantity(1)
                .unitPrice(30000L)
                .totalAmount(30000L)
                .paidAt(LocalDateTime.now().minusDays(10))
                .build();
        fundingContributionRepository.save(endedContribution);
    }

    // ==================== Funding 실제 DB 연동 테스트 ====================

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 실제 DB 데이터 검증")
    void getFundingParticipations_ReturnsRealData() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(2)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 통계 계산 검증")
    void getFundingParticipations_CalculatesStatistics() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - 통계 검증 (배송 제외)
        assertAll(
                () -> assertThat(result.getSummary().totalParticipations()).isEqualTo(2),
                () -> assertThat(result.getSummary().active()).isEqualTo(1),
                () -> assertThat(result.getSummary().ended()).isEqualTo(1)
                // TODO: 배송 CRUD 완성 후 추가
                // () -> assertThat(result.getSummary().fulfilling()).isEqualTo(0),
                // () -> assertThat(result.getSummary().fulfilled()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - participationNumber 포맷 검증")
    void getFundingParticipations_FormatsParticipationNumber() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - participationNumber 포맷 검증 (00010 형식)
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).participationNumber())
                        .matches("^\\d{5}$"),  // 5자리 숫자
                () -> assertThat(result.getContent().get(0).participationNumber().length())
                        .isEqualTo(5)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 매핑 검증")
    void getFundingParticipations_MapsStatus() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - 상태 매핑 검증 (OPEN → ACTIVE, SUCCESS → ENDED)
        FundingResponse.Participation activeParticipation = result.getContent().stream()
                .filter(p -> p.title().equals("진행중인 펀딩"))
                .findFirst()
                .orElseThrow();

        FundingResponse.Participation endedParticipation = result.getContent().stream()
                .filter(p -> p.title().equals("종료된 펀딩"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(activeParticipation.status()).isEqualTo("ACTIVE"),
                () -> assertThat(activeParticipation.statusText()).isEqualTo("진행중"),
                () -> assertThat(endedParticipation.status()).isEqualTo("ENDED"),
                () -> assertThat(endedParticipation.statusText()).isIn("종료", "성공", "실패", "취소")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - Meta 제외 검증")
    void getFundingParticipations_ExcludesMeta() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - Meta는 목록 조회에서 null
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).meta()).isNull(),
                () -> assertThat(result.getContent().get(1).meta()).isNull()
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 필드 타입 검증")
    void getFundingParticipations_ValidatesFieldTypes() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        FundingResponse.Participation participation = result.getContent().get(0);

        // Then - 타입 검증 (Long, String, int)
        assertAll(
                () -> assertThat(participation.participationNumber()).isInstanceOf(String.class),
                () -> assertThat(participation.participationId()).isInstanceOf(Long.class),
                () -> assertThat(participation.pledgedAmount()).isInstanceOf(Long.class),
                () -> assertThat(participation.artist().id()).isInstanceOf(Long.class),
                () -> assertThat(participation.quantity()).isInstanceOf(Integer.class)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 필터링 (ACTIVE)")
    void getFundingParticipations_FiltersActiveStatus() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, "ACTIVE", null, "paidAt", "DESC");

        // Then - ACTIVE만 조회
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("ACTIVE"),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("진행중인 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 필터링 (ENDED)")
    void getFundingParticipations_FiltersEndedStatus() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, "ENDED", null, "paidAt", "DESC");

        // Then - ENDED만 조회
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("ENDED"),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("종료된 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 키워드 검색 (제목)")
    void getFundingParticipations_SearchesByTitle() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, "진행중", "paidAt", "DESC");

        // Then - 제목으로 검색
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).title()).contains("진행중")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 키워드 검색 (작가명)")
    void getFundingParticipations_SearchesByArtistName() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, "테스트작가", "paidAt", "DESC");

        // Then - 작가명으로 검색
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).artist().name()).isEqualTo("테스트작가"),
                () -> assertThat(result.getContent().get(1).artist().name()).isEqualTo("테스트작가")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 정렬 (paidAt ASC)")
    void getFundingParticipations_SortsByPaidAtAsc() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "ASC");

        // Then - 오래된 순 정렬
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("종료된 펀딩"),
                () -> assertThat(result.getContent().get(1).title()).isEqualTo("진행중인 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 정렬 (paidAt DESC)")
    void getFundingParticipations_SortsByPaidAtDesc() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - 최근 순 정렬
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("진행중인 펀딩"),
                () -> assertThat(result.getContent().get(1).title()).isEqualTo("종료된 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 페이징 검증")
    void getFundingParticipations_HandlesPagination() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 1, null, null, "paidAt", "DESC");

        // Then - 페이징 처리
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getPage()).isEqualTo(0),
                () -> assertThat(result.getSize()).isEqualTo(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.isHasNext()).isTrue(),
                () -> assertThat(result.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - paidDate 필드 포맷 검증")
    void getFundingParticipations_FormatsPaidDate() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testAuthorization, 0, 10, null, null, "paidAt", "DESC");

        // Then - paidDate 포맷 검증 (YYYY-MM-DD)
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).paidDate())
                        .matches("^\\d{4}-\\d{2}-\\d{2}$")
        );
    }

    // TODO: 배송 CRUD 완성 후 추가할 테스트들
    /**
     * 배송 CRUD 완성 후 수정해야 할 테스트:
     *
     * 1. getFundingParticipations_CalculatesStatistics()
     *    - fulfilling, fulfilled 카운트 검증 주석 해제
     *
     * 2. 새로운 테스트 추가:
     *    - getFundingParticipations_IncludesDeliveryStatus()
     *      : 배송 상태 포함 여부 검증
     *    - getFundingParticipations_FiltersDeliveryStatus()
     *      : 배송 상태별 필터링 검증 (FULFILLING/FULFILLED)
     *
     * 3. Response DTO 수정:
     *    - FundingResponse.SummaryDto에서 배송 필드 주석 해제
     *    - (선택) Participation에 DeliveryInfo 필드 추가 시 검증 로직 추가
     */
}
