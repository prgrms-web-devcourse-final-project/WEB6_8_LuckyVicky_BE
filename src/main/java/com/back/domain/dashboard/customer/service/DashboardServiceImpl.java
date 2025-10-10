package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.request.*;
import com.back.domain.dashboard.customer.dto.response.*;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingContribution;
import com.back.domain.funding.entity.FundingImage;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 고객용 대시보드 서비스 구현체
 * 2025.10.02 수정 - JWT 표준 패턴 적용, Request DTO 활용
 * 2025.10.03 수정 - 주문일자 포맷팅 추가
 * 2025.10.08 수정 - 작가 신청 내역 조회 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final FundingContributionRepository fundingContributionRepository;
    private final UserRepository userRepository;
    private final com.back.domain.order.order.repository.OrderRepository orderRepository;
    private final com.back.domain.artist.repository.ArtistApplicationRepository artistApplicationRepository;

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");
    private static final DateTimeFormatter FUNDING_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");
    private static final DateTimeFormatter APPLICATION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public AccountResponse.Settings getAccountSettings(Long userId, String include) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        AccountResponse.Profile profile = null;
        AccountResponse.Contact contact = null;
        AccountResponse.Security security = null;

        // include 파라미터에 따라 필요한 정보만 포함
        String[] includeFields = include.split(",");

        for (String field : includeFields) {
            switch (field.trim()) {
                case "profile":
                    profile = new AccountResponse.Profile(
                            user.getId(),
                            user.getName(),
                            user.getProfileImageUrl());
                    break;
                case "contact":
                    contact = new AccountResponse.Contact(
                            user.getEmail(),
                            true,  // TODO: 이메일 인증 기능 추가 시 user.isEmailVerified() 사용
                            user.getPhone(),
                            user.getAddress());
                    break;
                case "security":
                    security = new AccountResponse.Security(
                            user.getCreateDate());  // TODO: 비밀번호 변경 날짜 추가 시 수정
                    break;
            }
        }

        return new AccountResponse.Settings(profile, contact, security);
    }

    @Override
    public ArtistApplicationResponse.List getArtistApplications(Long userId, ArtistApplicationSearchRequest request) {
        log.info("작가 신청 내역 조회 - userId: {}, page: {}, size: {}, status: {}",
                userId, request.page(), request.size(), request.status());

        // 1. 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new ServiceException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }

        // 2. 실제 DB에서 작가 신청 내역 조회
        List<com.back.domain.artist.entity.ArtistApplication> applications =
                artistApplicationRepository.findByUserIdOrderByCreateDateDesc(userId);

        // 3. 상태별 필터링 (status 파라미터가 있는 경우)
        if (request.status() != null && !request.status().isBlank()) {
            try {
                com.back.domain.artist.entity.ApplicationStatus filterStatus =
                        com.back.domain.artist.entity.ApplicationStatus.valueOf(request.status());
                applications = applications.stream()
                        .filter(app -> app.getStatus() == filterStatus)
                        .toList();
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상태 값: {}", request.status());
            }
        }

        // 4. 페이징 처리
        long total = applications.size();
        int start = request.page() * request.size();
        int end = Math.min(start + request.size(), applications.size());
        List<com.back.domain.artist.entity.ArtistApplication> pagedApplications =
                applications.subList(start, Math.min(end, applications.size()));

        // 5. DTO 변환
        List<ArtistApplicationResponse.Summary> content = pagedApplications.stream()
                .map(this::convertToApplicationSummary)
                .toList();

        // 6. 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) total / request.size());
        boolean hasNext = request.page() < totalPages - 1;
        boolean hasPrevious = request.page() > 0;

        return new ArtistApplicationResponse.List(
                null,  // summary는 필요없음 (Figma 디자인에 없음)
                content,
                request.page(), request.size(),
                total, totalPages,
                hasNext, hasPrevious);
    }

    /**
     * ArtistApplication 엔티티를 Summary DTO로 변환
     */
    private ArtistApplicationResponse.Summary convertToApplicationSummary(
            com.back.domain.artist.entity.ArtistApplication application) {

        String statusText = mapApplicationStatusText(application.getStatus());

        // 권한 정보: PENDING 상태일 때만 수정/취소 가능
        ArtistApplicationResponse.Permission permissions =
                new ArtistApplicationResponse.Permission(
                        application.isPending(),  // canEdit
                        application.isPending(),  // canCancel
                        application.isRejected()  // canAppeal (거절된 경우만 이의제기 가능)
                );

        return new ArtistApplicationResponse.Summary(
                application.getId(),
                application.getArtistName(),
                application.getCreateDate().format(APPLICATION_DATE_FORMATTER),
                application.getStatus().name(),
                statusText,
                permissions,
                application.getModifyDate() != null ? application.getModifyDate() : application.getCreateDate()
        );
    }

    /**
     * ApplicationStatus를 한글 텍스트로 매핑
     */
    private String mapApplicationStatusText(com.back.domain.artist.entity.ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "대기중";
            case APPROVED -> "승인";
            case REJECTED -> "거절";
            case CANCELLED -> "취소";
        };
    }

    @Override
    public ArtistApplicationResponse.Detail getArtistApplicationDetail(Long userId, Long applicationId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        // TODO: 신청자 본인 확인 로직 추가
        log.debug("입점 신청 상세 조회 - userId: {}, applicationId: {}", userId, applicationId);

        return new ArtistApplicationResponse.Detail(
                new ArtistApplicationResponse.Application(
                        applicationId, "PENDING", "검토중",
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now(),
                        null,
                        new ArtistApplicationResponse.Reviewer("admin_001", "관리자")),
                new ArtistApplicationResponse.Applicant(
                        "user123", "지원작가", "https://cdn.example.com/u/user123/profile.jpg"),
                new ArtistApplicationResponse.Contact(
                        "user123@example.com", "010-1234-5678"),
                new ArtistApplicationResponse.Business(
                        "123-45-67890",
                        new ArtistApplicationResponse.FileDto(
                                "biz-123", "사업자등록증.pdf",
                                "https://files.example.com/biz-123",
                                LocalDateTime.now()),
                        "2025-서울-1234",
                        new ArtistApplicationResponse.FileDto(
                                "rep-456", "통신판매업신고증.pdf",
                                "https://files.example.com/rep-456",
                                LocalDateTime.now()),
                        "서울특별시 강남구"),
                new ArtistApplicationResponse.Profile(
                        "작가 소개입니다.",
                        Arrays.asList("스티커", "메모지"),
                        List.of(new ArtistApplicationResponse.Sns("Instagram", "@artist")),
                        List.of(new ArtistApplicationResponse.FileDto(
                                "pf-1", "포트폴리오.pdf",
                                "https://files.example.com/pf-1",
                                LocalDateTime.now()))),
                new ArtistApplicationResponse.Permission(true, true, true)
        );
    }

    @Override
    public OrderResponse.List getOrders(Long userId, OrderSearchRequest request) {
        log.debug("주문 목록 조회 - userId: {}, request: {}", userId, request);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        // 2. 주문 목록 조회 (상품명 정렬 여부에 따라 다른 메서드 사용)
        Page<com.back.domain.order.order.entity.Order> orderPage;

        if ("productName".equals(request.sort())) {
            // 상품명 정렬: 별도 쿼리 사용 (Pageable의 정렬은 무시됨)
            String direction = request.order() != null ? request.order() : "DESC";
            Pageable pageable = PageRequest.of(request.page(), request.size());
            orderPage = orderRepository.findOrdersForDashboardSortedByProductName(
                    user, request.keyword(), direction, pageable);
        } else {
            // 기본 정렬: Pageable의 정렬 사용
            Pageable pageable = createOrderPageable(request);
            orderPage = orderRepository.findOrdersForDashboard(user, request.keyword(), pageable);
        }

        // 3. 빈 결과 처리
        List<OrderResponse.Summary> content;
        if (orderPage.isEmpty()) {
            content = List.of();
        } else {
            // 4. 조회된 주문 ID 목록 추출
            List<Long> orderIds = orderPage.getContent().stream()
                    .map(com.back.domain.order.order.entity.Order::getId)
                    .collect(Collectors.toList());

            // 5. 주문 상세 정보 조회 (OrderItem, Product, Images 포함)
            List<com.back.domain.order.order.entity.Order> ordersWithDetails =
                    orderRepository.findOrdersWithDetailsById(orderIds);

            // 6. 정렬 순서 유지를 위한 Map 생성
            java.util.Map<Long, com.back.domain.order.order.entity.Order> orderMap = ordersWithDetails.stream()
                    .collect(Collectors.toMap(
                            com.back.domain.order.order.entity.Order::getId,
                            order -> order
                    ));

            // 7. 원래 페이징 순서대로 엔티티 정렬 및 DTO 변환
            content = orderPage.getContent().stream()
                    .map(order -> orderMap.get(order.getId()))
                    .filter(java.util.Objects::nonNull)
                    .map(this::convertToOrderSummary)
                    .collect(Collectors.toList());
        }

        // 8. 통계 계산
        OrderResponse.SummaryDto summary = new OrderResponse.SummaryDto(
                (int) orderPage.getTotalElements()
        );

        // 9. 응답 생성
        return new OrderResponse.List(
                summary, content,
                orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(),
                orderPage.hasNext(), orderPage.hasPrevious()
        );
    }

    /**
     * 주문용 Pageable 생성 (정렬 포함)
     * productName 정렬은 별도 쿼리에서 처리하므로 여기서는 제외
     */
    private Pageable createOrderPageable(OrderSearchRequest request) {
        String sort = request.sort() != null ? request.sort() : "orderDate";
        String order = request.order() != null ? request.order() : "DESC";

        org.springframework.data.domain.Sort.Direction direction =
                "ASC".equalsIgnoreCase(order) ?
                        org.springframework.data.domain.Sort.Direction.ASC :
                        org.springframework.data.domain.Sort.Direction.DESC;

        // 정렬 필드 매핑
        String sortField = switch (sort) {
            case "totalAmount" -> "totalAmount";
            case "status" -> "status";
            default -> "orderDate"; // productName은 여기서 처리하지 않음
        };

        return PageRequest.of(request.page(), request.size(),
                org.springframework.data.domain.Sort.by(direction, sortField));
    }

    /**
     * Order 엔티티를 OrderResponse.Summary로 변환
     */
    private OrderResponse.Summary convertToOrderSummary(com.back.domain.order.order.entity.Order order) {
        List<com.back.domain.order.orderItem.entity.OrderItem> orderItems = order.getOrderItems();

        // 화면 표시용 주문번호 생성 (7자리 포맷)
        String displayOrderNumber = String.format("%07d", order.getId());

        // 대표 상품 (첫 번째 상품)
        OrderResponse.Product representativeProduct = null;
        if (!orderItems.isEmpty()) {
            com.back.domain.order.orderItem.entity.OrderItem firstItem = orderItems.get(0);
            representativeProduct = convertToProductDto(firstItem);
        }

        // 모든 주문 상품 변환
        List<OrderResponse.OrderItem> items = orderItems.stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());

        // 배송 정보
        OrderResponse.Shipping shipping = new OrderResponse.Shipping(
                order.getShippingAddress1() != null ? order.getShippingAddress1() : "",
                order.getRecipientName() != null ? order.getRecipientName() : ""
        );

        // 주문 상태 매핑 (4가지 상태만)
        String status = mapOrderStatus(order.getStatus());
        String statusText = mapOrderStatusText(order.getStatus());

        // 권한 정보 (결제완료일 때만 취소 가능)
        OrderResponse.Permission permissions = new OrderResponse.Permission(
                order.getStatus() == com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED,
                false, // 반품 기능 제거
                false  // 교환 기능 제거
        );

        // 링크 정보 (실제 orderNumber 사용)
        OrderResponse.Link links = new OrderResponse.Link("/orders/" + order.getOrderNumber());

        return new OrderResponse.Summary(
                order.getId().toString(),
                displayOrderNumber,  // 화면에는 7자리 포맷팅된 번호 표시
                order.getOrderDate().format(ORDER_DATE_FORMATTER),  // "2025. 09. 18" 형식
                status,
                statusText,
                order.getTotalAmount().intValue(),
                orderItems.size(),
                representativeProduct,
                shipping,
                null,  // A/S 정보 제거
                permissions,
                links,
                items
        );
    }

    /**
     * OrderItem을 Product DTO로 변환
     */
    private OrderResponse.Product convertToProductDto(com.back.domain.order.orderItem.entity.OrderItem orderItem) {
        com.back.domain.product.product.entity.Product product = orderItem.getProduct();

        return new OrderResponse.Product(
                product.getId(),
                product.getName(),
                orderItem.getQuantity(),
                orderItem.getPrice().intValue(),
                getProductThumbnailUrl(product)
        );
    }

    /**
     * OrderItem을 OrderItem DTO로 변환
     */
    private OrderResponse.OrderItem convertToOrderItemDto(com.back.domain.order.orderItem.entity.OrderItem orderItem) {
        com.back.domain.product.product.entity.Product product = orderItem.getProduct();

        return new OrderResponse.OrderItem(
                orderItem.getId(),
                product.getId(),
                product.getName(),
                orderItem.getQuantity(),
                orderItem.getPrice().intValue(),
                getProductThumbnailUrl(product)
        );
    }

    /**
     * 상품 썸네일 이미지 URL 조회
     * 삭제된 상품이거나 이미지가 없으면 null 반환
     */
    private String getProductThumbnailUrl(com.back.domain.product.product.entity.Product product) {
        // 삭제된 상품은 이미지 null
        if (product.isDeleted()) {
            return null;
        }

        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(image -> "THUMBNAIL".equals(image.getFileType().name()))
                .findFirst()
                .map(com.back.domain.product.product.entity.ProductImage::getFileUrl)
                .orElse(null);
    }

    /**
     * OrderStatus를 프론트엔드용 상태 코드로 매핑 (4가지 상태만)
     * 주문/배송 조회에서는 이 4가지 상태만 조회되므로 default 케이스는 발생하지 않음
     */
    private String mapOrderStatus(com.back.domain.order.order.entity.OrderStatus status) {
        return switch (status) {
            case PAYMENT_COMPLETED -> "PAYMENT_COMPLETED";
            case PREPARING_SHIPMENT -> "PREPARING";
            case SHIPPING -> "SHIPPING";
            case DELIVERED -> "DELIVERED";
            default -> throw new IllegalArgumentException("주문/배송 조회에서 허용되지 않는 상태: " + status);
        };
    }

    /**
     * OrderStatus를 한글 텍스트로 매핑 (4가지 상태만)
     * 주문/배송 조회에서는 이 4가지 상태만 조회되므로 default 케이스는 발생하지 않음
     */
    private String mapOrderStatusText(com.back.domain.order.order.entity.OrderStatus status) {
        return switch (status) {
            case PAYMENT_COMPLETED -> "결제완료";
            case PREPARING_SHIPMENT -> "배송준비중";
            case SHIPPING -> "배송중";
            case DELIVERED -> "배송완료";
            default -> throw new IllegalArgumentException("주문/배송 조회에서 허용되지 않는 상태: " + status);
        };
    }

    @Override
    public FollowingResponse.List getFollowingArtists(Long userId, FollowingSearchRequest request) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        log.debug("팔로우한 작가 목록 조회 - userId: {}, request: {}", userId, request);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        FollowingResponse.Profile profile = new FollowingResponse.Profile(
                user.getId().toString(),
                user.getName(),
                user.getProfileImageUrl());

        FollowingResponse.SummaryDto summary =
                new FollowingResponse.SummaryDto(5);

        List<FollowingResponse.Artist> content = Arrays.asList(
                new FollowingResponse.Artist(
                        "artist_001", "감성작가",
                        "https://cdn.example.com/artists/artist_001/profile.jpg",
                        500, "/artists/artist_001",
                        new FollowingResponse.FollowRelation("FOLLOWING", LocalDateTime.now()),
                        new FollowingResponse.Badge(true)
                ),
                new FollowingResponse.Artist(
                        "artist_002", "캐릭터작가",
                        "https://cdn.example.com/artists/artist_002/profile.jpg",
                        123, "/artists/artist_002",
                        new FollowingResponse.FollowRelation("FOLLOWING", LocalDateTime.now().minusDays(1)),
                        new FollowingResponse.Badge(false)
                )
        );

        return new FollowingResponse.List(
                profile, summary, content,
                request.page(), request.size(),
                5, 1, false, false);
    }

    @Override
    public WishlistResponse.List getWishlist(Long userId, WishlistSearchRequest request) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        log.debug("찜한 상품 목록 조회 - userId: {}, request: {}", userId, request);

        WishlistResponse.SummaryDto summary = new WishlistResponse.SummaryDto(15);

        List<WishlistResponse.BulkAction> bulkActions = List.of(
                new WishlistResponse.BulkAction("BULK_UNWISH", "선택 항목 해제", true)
        );

        List<WishlistResponse.Item> content = List.of(
                new WishlistResponse.Item(
                        "w-001", 123157L, "0123157", "감성 일러스트 포스터", 25000,
                        new WishlistResponse.Artist("artist001", "감성작가"),
                        "https://cdn.example.com/p/123157/main.jpg", "SELLING", "2025-09-18",
                        LocalDateTime.now(), "/products/0123157",
                        new WishlistResponse.Permission(true)
                ),
                new WishlistResponse.Item(
                        "w-002", 123158L, "0123158", "귀여운 스티커 세트", 15000,
                        new WishlistResponse.Artist("artist002", "캐릭터작가"),
                        "https://cdn.example.com/p/123158/main.jpg", "SELLING", "2025-09-17",
                        LocalDateTime.now().minusDays(1), "/products/0123158",
                        new WishlistResponse.Permission(true)
                )
        );

        return new WishlistResponse.List(
                summary, bulkActions, content,
                request.page(), request.size(),
                15, 2, true, false);
    }

    @Override
    public FundingResponse.List getFundingParticipations(Long userId, FundingSearchRequest request) {
        log.debug("펀딩 목록 조회 - userId: {}, request: {}", userId, request);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        // 2. Pageable 생성 (정렬 포함)
        Pageable pageable = createFundingPageable(request);

        // 3. 펀딩 참여 목록 조회
        Page<FundingContribution> contributionPage = fundingContributionRepository
                .findContributionsByBuyerWithFilters(
                        userId,
                        request.keyword(),
                        request.status(),
                        pageable);

        // 4. 엔티티 → DTO 변환
        List<FundingResponse.Participation> content = contributionPage.getContent().stream()
                .map(this::toParticipationDto)
                .collect(Collectors.toList());

        // 5. 응답 생성 (summary는 null - 프론트 요구사항에 통계 없음)
        return new FundingResponse.List(
                null,  // summary는 프론트 확정 시까지 null
                content,
                contributionPage.getNumber(),
                contributionPage.getSize(),
                contributionPage.getTotalElements(),
                contributionPage.getTotalPages(),
                contributionPage.hasNext(),
                contributionPage.hasPrevious()
        );
    }

    /**
     * 펀딩용 Pageable 생성 (정렬 포함)
     */
    private Pageable createFundingPageable(FundingSearchRequest request) {
        String sort = request.sort() != null ? request.sort() : "paidAt";
        String order = request.order() != null ? request.order() : "DESC";

        org.springframework.data.domain.Sort.Direction direction =
                "ASC".equalsIgnoreCase(order) ?
                        org.springframework.data.domain.Sort.Direction.ASC :
                        org.springframework.data.domain.Sort.Direction.DESC;

        // 정렬 필드 매핑 (DTO 필드명 → 엔티티 필드명)
        String sortField = switch (sort) {
            case "pledgedAmount" -> "totalAmount";
            case "title" -> "funding.title";
            case "artistName" -> "funding.user.name";
            case "status" -> "funding.status";
            default -> "paidAt";
        };

        return PageRequest.of(request.page(), request.size(),
                org.springframework.data.domain.Sort.by(direction, sortField));
    }

    /**
     * FundingContribution 엔티티를 Participation DTO로 변환
     */
    private FundingResponse.Participation toParticipationDto(FundingContribution contribution) {
        Funding funding = contribution.getFunding();

        return new FundingResponse.Participation(
                generateParticipationNumber(contribution.getId()),
                contribution.getId(),
                getFundingThumbnailUrl(funding),
                funding.getTitle(),
                new FundingResponse.Artist(
                        funding.getUser().getId(),
                        funding.getUser().getName()
                ),
                contribution.getQuantity(),
                contribution.getTotalAmount(),
                mapFundingStatus(funding.getStatus()),
                mapFundingStatusText(funding.getStatus()),
                contribution.getPaidAt().format(FUNDING_DATE_FORMATTER),  // "2025. 09. 18" 형식
                new FundingResponse.Link("/fundings/" + funding.getId()),
                null  // Meta는 목록 조회에서 제외
        );
    }

    /**
     * 펀딩 썸네일 이미지 URL 조회
     * 우선순위: THUMBNAIL > imageUrl
     */
    private String getFundingThumbnailUrl(Funding funding) {
        try {
            // images 컬렉션에서 THUMBNAIL 타입 찾기
            if (funding.getImages() != null && !funding.getImages().isEmpty()) {
                String thumbnailUrl = funding.getImages().stream()
                        .filter(image -> image != null && "THUMBNAIL".equals(image.getFileType().name()))
                        .findFirst()
                        .map(FundingImage::getFileUrl)
                        .orElse(null);

                if (thumbnailUrl != null) {
                    return thumbnailUrl;
                }
            }
        } catch (Exception e) {
            // LazyInitializationException 등의 에러 발생 시 imageUrl 사용
            log.warn("펀딩 이미지 컬렉션 접근 실패, imageUrl 사용 - fundingId: {}", funding.getId(), e);
        }

        // THUMBNAIL이 없거나 에러 발생 시 imageUrl 사용
        return funding.getImageUrl();
    }

    /**
     * 참여번호 생성 (5자리 포맷)
     */
    private String generateParticipationNumber(Long id) {
        return String.format("%05d", id);
    }

    /**
     * Funding 상태를 프론트엔드용 상태로 매핑
     */
    private String mapFundingStatus(FundingStatus status) {
        return switch (status) {
            case PENDING, APPROVED -> "UPCOMING";
            case  OPEN -> "ACTIVE";
            case CLOSED, SUCCESS, FAILED, CANCELED, REJECTED -> "ENDED";
        };
    }

    /**
     * Funding 상태를 한글 텍스트로 매핑
     */
    private String mapFundingStatusText(FundingStatus status) {
        return switch (status) {
            case PENDING -> "심사중";
            case APPROVED -> "승인됨";
            case REJECTED -> "심사거절";
            case OPEN -> "진행중";
            case CLOSED -> "종료";
            case SUCCESS -> "성공";
            case FAILED -> "실패";
            case CANCELED -> "취소";
        };
    }

    @Override
    public CashResponse.Balance getCashBalance(Long userId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        log.debug("캐시 정보 조회 - userId: {}", userId);

        return new CashResponse.Balance(5900, "KRW", LocalDateTime.now());
    }

    @Override
    public CashResponse.HistoryList getCashHistory(Long userId, CashHistorySearchRequest request) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        log.debug("캐시 충전 내역 조회 - userId: {}, request: {}", userId, request);

        CashResponse.SummaryDto summary = new CashResponse.SummaryDto(5, 15000, 2);

        List<CashResponse.Transaction> content = Arrays.asList(
                new CashResponse.Transaction(
                        "RC-001", LocalDateTime.now(),
                        "캐시 충전", 10000, 50, "NAVERPAY", "네이버페이", "COMPLETED",
                        new CashResponse.Link(null)),
                new CashResponse.Transaction(
                        "RC-002", LocalDateTime.now().minusDays(1),
                        "캐시 사용", -5000, 45, "PURCHASE", "상품구매", "COMPLETED",
                        new CashResponse.Link("/orders/0123157"))
        );

        return new CashResponse.HistoryList(
                summary, content,
                request.page(), request.size(),
                5, 1, false, false);
    }
}
