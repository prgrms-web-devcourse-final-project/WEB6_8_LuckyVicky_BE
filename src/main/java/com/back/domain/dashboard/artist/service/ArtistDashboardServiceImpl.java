package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 작가용 대시보드 서비스 구현체
 * 2025.09.22 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistDashboardServiceImpl implements ArtistDashboardService {

    @Override
    public ArtistMainResponse getMainStats(String authorization, String range, String from, String to,
                                           String interval, String tz) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 통계 데이터 조회

        // 프로필 정보
        ArtistMainResponse.Profile profile = ArtistMainResponse.Profile.builder()
                .userId(5L)
                .nickname("감성작가")
                .email("artist@example.com")
                .profileImageUrl("https://cdn.example.com/u/5/profile.jpg")
                .build();

        // 통계 정보
        ArtistMainResponse.Stats stats = ArtistMainResponse.Stats.builder()
                .followerCount(1250)
                .productCount(28)
                .todaysSales(125000)
                .todaysOrders(8)
                .totalSales(2450000)
                .totalOrders(156)
                .averageRating(4.8)
                .pendingOrders(3)
                .build();

        // 트렌드 메타 정보
        ArtistMainResponse.Meta meta = ArtistMainResponse.Meta.builder()
                .range("6M")
                .from("2025-03-01")
                .to("2025-09-01")
                .interval("WEEK")
                .timezone("Asia/Seoul")
                .maxPoints(400)
                .compare(ArtistMainResponse.Compare.builder()
                        .from("2024-09-01")
                        .to("2025-03-01")
                        .build())
                .build();

        // 시계열 데이터 포인트
        List<ArtistMainResponse.DataPoint> salesPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-03-03", 95000),
                new ArtistMainResponse.DataPoint("2025-03-10", 140000),
                new ArtistMainResponse.DataPoint("2025-03-17", 110000),
                new ArtistMainResponse.DataPoint("2025-03-24", 180000),
                new ArtistMainResponse.DataPoint("2025-03-31", 125000),
                new ArtistMainResponse.DataPoint("2025-04-07", 165000),
                new ArtistMainResponse.DataPoint("2025-04-14", 125000)
        );

        List<ArtistMainResponse.DataPoint> orderPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-03-03", 5),
                new ArtistMainResponse.DataPoint("2025-03-10", 8),
                new ArtistMainResponse.DataPoint("2025-03-17", 6),
                new ArtistMainResponse.DataPoint("2025-03-24", 12),
                new ArtistMainResponse.DataPoint("2025-03-31", 7),
                new ArtistMainResponse.DataPoint("2025-04-07", 9),
                new ArtistMainResponse.DataPoint("2025-04-14", 8)
        );

        List<ArtistMainResponse.DataPoint> followerPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-03-03", 1180),
                new ArtistMainResponse.DataPoint("2025-03-10", 1195),
                new ArtistMainResponse.DataPoint("2025-03-17", 1210),
                new ArtistMainResponse.DataPoint("2025-03-24", 1225),
                new ArtistMainResponse.DataPoint("2025-03-31", 1235),
                new ArtistMainResponse.DataPoint("2025-04-07", 1245),
                new ArtistMainResponse.DataPoint("2025-04-14", 1250)
        );

        // 시계열 데이터
        ArtistMainResponse.Series series = ArtistMainResponse.Series.builder()
                .sales(ArtistMainResponse.SeriesData.builder()
                        .unit("KRW")
                        .points(salesPoints)
                        .total(2450000)
                        .build())
                .orders(ArtistMainResponse.SeriesData.builder()
                        .unit("COUNT")
                        .points(orderPoints)
                        .total(156)
                        .build())
                .followers(ArtistMainResponse.SeriesData.builder()
                        .unit("COUNT")
                        .points(followerPoints)
                        .total(1250)
                        .build())
                .build();

        // 변화량 정보
        ArtistMainResponse.Changes changes = ArtistMainResponse.Changes.builder()
                .sales(new ArtistMainResponse.ChangeData(-40000, -0.242))
                .orders(new ArtistMainResponse.ChangeData(1, 0.143))
                .followers(new ArtistMainResponse.ChangeData(70, 0.059))
                .build();

        // 트렌드 정보
        ArtistMainResponse.Trends trends = ArtistMainResponse.Trends.builder()
                .meta(meta)
                .series(series)
                .changes(changes)
                .build();

        // 알림 정보
        List<ArtistMainResponse.Alert> orderAlerts = Arrays.asList(
                ArtistMainResponse.Alert.builder()
                        .type("NEW_ORDER")
                        .message("새로운 주문 3건이 접수되었습니다")
                        .count(3)
                        .timestamp(LocalDateTime.of(2025, 9, 18, 14, 30))
                        .build(),
                ArtistMainResponse.Alert.builder()
                        .type("URGENT_ORDER")
                        .message("긴급 처리가 필요한 주문이 있습니다")
                        .count(1)
                        .timestamp(LocalDateTime.of(2025, 9, 18, 13, 15))
                        .build()
        );

        List<ArtistMainResponse.Alert> fundingAlerts = List.of(
                ArtistMainResponse.Alert.builder()
                        .type("FUNDING_GOAL_ACHIEVED")
                        .message("펀딩 목표가 달성되었습니다")
                        .count(1)
                        .timestamp(LocalDateTime.of(2025, 9, 18, 12, 0))
                        .build()
        );

        ArtistMainResponse.Notifications notifications = ArtistMainResponse.Notifications.builder()
                .orderAlerts(orderAlerts)
                .fundingAlerts(fundingAlerts)
                .build();

        return ArtistMainResponse.builder()
                .profile(profile)
                .stats(stats)
                .trends(trends)
                .notifications(notifications)
                .serverTime(LocalDateTime.of(2025, 12, 24, 15, 0))
                .timezone("Asia/Seoul")
                .build();
    }
    
    @Override
    public ArtistProductResponse.List getProducts(String authorization, int page, int size, String keyword,
                                                 Boolean selling, String sort, String order) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 상품 목록 조회
        
        List<ArtistProductResponse.Product> content = Arrays.asList(
                ArtistProductResponse.Product.builder()
                        .productNumber("0123157")
                        .productName("상품명입니다 상품명입니다")
                        .price(90000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025. 09. 18")
                        .build(),
                ArtistProductResponse.Product.builder()
                        .productNumber("0123156")
                        .productName("상품명입니다 상품명입니다")
                        .price(90000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025. 09. 18")
                        .build(),
                ArtistProductResponse.Product.builder()
                        .productNumber("0123155")
                        .productName("상품명입니다 상품명입니다")
                        .price(90000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025. 09. 18")
                        .build()
        );
        
        return new ArtistProductResponse.List(content, page, 10, 28, 3, true, false);
    }
    
    @Override
    public ArtistCashResponse.Balance getCashBalance(String authorization) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 지갑 잔액 정보 조회
        
        return ArtistCashResponse.Balance.builder()
                .currentBalance(72000)
                .pendingSettlement(15000)
                .pendingWithdrawal(0)
                .withdrawable(72000)
                .currency("KRW")
                .updatedAt(LocalDateTime.of(2025, 9, 24, 10, 0))
                .build();
    }
}
