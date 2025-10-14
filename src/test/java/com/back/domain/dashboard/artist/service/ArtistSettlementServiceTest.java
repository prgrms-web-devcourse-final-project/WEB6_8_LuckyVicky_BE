package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.request.ArtistSettlementSearchRequest;
import com.back.domain.dashboard.artist.dto.response.ArtistSettlementResponse;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.payment.settlement.entity.Settlement;
import com.back.domain.payment.settlement.repository.SettlementRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArtistSettlementServiceTest {

    @Autowired
    private ArtistDashboardService artistDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    private User artist;
    private MoriCashBalance balance;

    @BeforeEach
    void setUp() {
        // 작가 생성
        artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();
        artist = userRepository.save(artist);

        // 모리캐시 잔액 생성 및 충분한 금액 충전
        balance = MoriCashBalance.createInitialBalance(artist);
        balance.addBalance(1000000); // 정산할 수 있도록 충분한 금액 충전
        balance = moriCashBalanceRepository.save(balance);

        // 정산 데이터 생성
        createSettlementData();
    }

    private void createSettlementData() {
        // 정산 데이터 5개 생성 (모두 현재 시간으로 생성됨)
        for (int i = 0; i < 5; i++) {
            int amount = 30000 + (i * 10000); // 30k, 40k, 50k, 60k, 70k
            Settlement settlement = Settlement.builder()
                    .artist(artist)
                    .requestedAmount(amount)
                    .commissionRate(10)  // 사용되지 않음 (환전 시 수수료 없음)
                    .bankName("데모은행")
                    .accountNumber("000-0000-0000")
                    .accountHolder(artist.getName())
                    .build();
            
            settlement = settlementRepository.save(settlement);
            
            System.out.println("=== Created Settlement ===");
            System.out.println("ID: " + settlement.getId());
            System.out.println("Amount: " + settlement.getRequestedAmount());
            System.out.println("Commission: " + settlement.getCommissionAmount());
            System.out.println("Net: " + settlement.getNetAmount());
            System.out.println("Status: " + settlement.getStatus());
            System.out.println("CompletedAt: " + settlement.getCompletedAt());

            // MoriCashBalance 통계 업데이트 (환전 처리)
            balance.processSettlement(amount);
        }
        
        moriCashBalanceRepository.save(balance);
        
        // 저장된 데이터 확인
        List<Settlement> savedSettlements = settlementRepository.findAll();
        System.out.println("=== Total Settlements in DB: " + savedSettlements.size() + " ===");
    }

    @Test
    @DisplayName("정산 현황 조회 - 기본 조회")
    void getSettlements_Basic() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        System.out.println("=== Test Setup ===");
        System.out.println("Artist ID: " + artist.getId());
        System.out.println("Request Year: " + request.year());
        System.out.println("Current Year: " + LocalDate.now().getYear());
        
        // 실제 저장된 데이터 확인
        List<Settlement> allSettlements = settlementRepository.findAll();
        System.out.println("Total settlements in DB: " + allSettlements.size());
        allSettlements.forEach(s -> {
            System.out.println("Settlement: ID=" + s.getId() + 
                             ", Artist=" + s.getArtist().getId() + 
                             ", Amount=" + s.getRequestedAmount() +
                             ", Commission=" + s.getCommissionAmount() +
                             ", Net=" + s.getNetAmount() +
                             ", CompletedAt=" + s.getCompletedAt());
        });

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        System.out.println("=== Response ===");
        System.out.println("Summary - TotalSales: " + response.summary().totalSales().amount());
        System.out.println("Summary - Commission: " + response.summary().totalCommission().amount());
        System.out.println("Summary - NetIncome: " + response.summary().totalNetIncome().amount());
        System.out.println("Table - Content Size: " + response.table().getContent().size());
        System.out.println("Table - Total Elements: " + response.table().getTotalElements());

        // then
        assertThat(response).isNotNull();
        assertThat(response.scope().year()).isEqualTo(LocalDate.now().getYear());
        assertThat(response.scope().month()).isNull();

        // 요약 정보 확인 (환전 시 수수료 없음)
        assertThat(response.summary().totalSales().amount()).isEqualTo(250000); // 30k + 40k + 50k + 60k + 70k
        assertThat(response.summary().totalCommission().amount()).isEqualTo(0); // 환전 시 수수료 없음!
        assertThat(response.summary().totalNetIncome().amount()).isEqualTo(250000); // 수수료 없으므로 총액과 동일

        // 테이블 데이터 확인
        assertThat(response.table().getContent()).hasSize(5);
        assertThat(response.table().getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("정산 현황 조회 - 특정 월")
    void getSettlements_Month() {
        // given
        int currentMonth = LocalDate.now().getMonthValue();
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), currentMonth, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.scope().year()).isEqualTo(LocalDate.now().getYear());
        assertThat(response.scope().month()).isEqualTo(currentMonth);

        // 테이블 데이터 확인 (이번 달 데이터만)
        assertThat(response.table().getContent()).hasSize(5);
        assertThat(response.table().getContent())
                .allMatch(s -> s.statusText().equals("정산완료"));
    }

    @Test
    @DisplayName("정산 현황 조회 - 정렬 (매출액 오름차순)")
    void getSettlements_SortByAmount() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "grossAmount", "ASC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();

        // 첫 번째가 가장 작은 금액
        assertThat(content.get(0).grossAmount()).isEqualTo(30000);
        
        // 금액이 오름차순인지 확인
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).grossAmount()).isLessThanOrEqualTo(content.get(i + 1).grossAmount());
        }
    }

    @Test
    @DisplayName("정산 현황 조회 - 정렬 (매출액 내림차순)")
    void getSettlements_SortByAmountDesc() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "grossAmount", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();

        // 첫 번째가 가장 큰 금액
        assertThat(content.get(0).grossAmount()).isEqualTo(70000);
        
        // 금액이 내림차순인지 확인
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).grossAmount()).isGreaterThanOrEqualTo(content.get(i + 1).grossAmount());
        }
    }

    @Test
    @DisplayName("정산 현황 조회 - 페이징")
    void getSettlements_Paging() {
        // given - 첫 페이지
        ArtistSettlementSearchRequest request1 = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 2, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response1 = artistDashboardService.getSettlements(artist.getId(), request1);

        // then
        assertThat(response1.table().getContent()).hasSize(2);
        assertThat(response1.table().isHasNext()).isTrue();
        assertThat(response1.table().isHasPrevious()).isFalse();

        // given - 두 번째 페이지
        ArtistSettlementSearchRequest request2 = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 1, 2, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response2 = artistDashboardService.getSettlements(artist.getId(), request2);

        // then
        assertThat(response2.table().getContent()).hasSize(2);
        assertThat(response2.table().isHasNext()).isTrue();
        assertThat(response2.table().isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("정산 현황 조회 - 모든 상태가 정산완료")
    void getSettlements_AllCompleted() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();
        assertThat(content).allMatch(s -> s.status().equals("COMPLETED"));
        assertThat(content).allMatch(s -> s.statusText().equals("정산완료"));
    }

    @Test
    @DisplayName("정산 현황 조회 - 수수료 계산 확인")
    void getSettlements_CommissionCalculation() {
        // given
        int currentMonth = LocalDate.now().getMonthValue();
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), currentMonth, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();

        // 각 정산의 수수료와 순수익 확인 (환전 시 수수료 없음)
        content.forEach(settlement -> {
            assertThat(settlement.commission()).isEqualTo(0);  // 환전 시 수수료 없음
            assertThat(settlement.netAmount()).isEqualTo(settlement.grossAmount());  // 총액 = 순수익
        });
    }

    @Test
    @DisplayName("정산 현황 조회 - 차트 데이터 생성 확인")
    void getSettlements_ChartData() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response.chart()).isNotNull();
        assertThat(response.chart().series()).isNotNull();
        assertThat(response.chart().series().sales()).hasSize(12); // 1-12월
        assertThat(response.chart().yDomain().min()).isEqualTo(0);
        assertThat(response.chart().yDomain().max()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("정산 현황 조회 - 데이터 없는 월")
    void getSettlements_EmptyMonth() {
        // given - 데이터가 없는 다음 달
        int nextMonth = (LocalDate.now().getMonthValue() % 12) + 1;
        int year = nextMonth == 1 ? LocalDate.now().getYear() + 1 : LocalDate.now().getYear();
        
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                year, nextMonth, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response.table().getContent()).isEmpty();
        assertThat(response.table().getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("정산 현황 조회 - 응답 구조 검증")
    void getSettlements_ResponseStructure() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        // Scope 검증
        assertThat(response.scope()).isNotNull();
        assertThat(response.scope().year()).isNotNull();
        
        // Summary 검증
        assertThat(response.summary()).isNotNull();
        assertThat(response.summary().totalSales()).isNotNull();
        assertThat(response.summary().totalCommission()).isNotNull();
        assertThat(response.summary().totalNetIncome()).isNotNull();
        
        // Chart 검증
        assertThat(response.chart()).isNotNull();
        assertThat(response.chart().series()).isNotNull();
        assertThat(response.chart().yDomain()).isNotNull();
        
        // Table 검증
        assertThat(response.table()).isNotNull();
        assertThat(response.table().getContent()).isNotNull();

        // 기타 필드 검증
        assertThat(response.timezone()).isEqualTo("Asia/Seoul");
        assertThat(response.serverTime()).isNotNull();
    }
}
