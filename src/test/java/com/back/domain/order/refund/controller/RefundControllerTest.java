package com.back.domain.order.refund.controller;

import com.back.domain.order.refund.dto.request.RefundRequestDto;
import com.back.domain.order.refund.dto.response.RefundResponseDto;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.service.RefundService;
import com.back.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RefundControllerTest {

    private RefundController refundController;
    private RefundService refundService;
    private ObjectMapper objectMapper;

    private User testUser;
    private RefundRequestDto refundRequestDto;
    private RefundResponseDto refundResponseDto;

    @BeforeEach
    void setUp() throws Exception {
        // Mock 서비스 설정
        refundService = mock(RefundService.class);
        objectMapper = new ObjectMapper();
        refundController = new RefundController(refundService);

        // 테스트 사용자 설정 (reflection으로 ID 설정)
        testUser = User.createLocalUser("test@example.com", "password123", "테스트사용자", "010-1234-5678");
        java.lang.reflect.Field idField = testUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, 1L);

        // 환불 요청 DTO
        refundRequestDto = new RefundRequestDto(
                1L, // orderId
                List.of(1L, 2L), // orderItemIds
                "단순변심", // reason
                "색상이 마음에 들지 않음", // detailReason
                BigDecimal.valueOf(50000), // refundAmount
                Refund.RefundMethod.BANK_TRANSFER, // refundMethod
                List.of("image1.jpg", "image2.jpg") // attachmentFiles
        );

        // 환불 응답 DTO
        refundResponseDto = new RefundResponseDto(
                1L, // refundId
                1L, // orderId
                "ORD123456789", // orderNumber
                Refund.RefundStatus.REQUESTED, // status
                "단순변심", // reason
                "색상이 마음에 들지 않음", // detailReason
                BigDecimal.valueOf(50000), // refundAmount
                Refund.RefundMethod.BANK_TRANSFER, // refundMethod
                List.of("image1.jpg", "image2.jpg"), // attachmentFiles
                LocalDateTime.now(), // createdAt
                LocalDateTime.now(), // updatedAt
                List.of( // refundItems
                        new RefundResponseDto.RefundItemResponseDto(
                                1L, // refundItemId
                                1L, // orderItemId
                                "테스트 상품", // productName
                                "http://example.com/image.jpg", // productThumbnailUrl
                                2, // quantity
                                BigDecimal.valueOf(25000), // refundPrice
                                BigDecimal.valueOf(50000), // totalRefundAmount
                                "빨강, L" // optionInfo
                        )
                )
        );
    }

    @Test
    @DisplayName("환불 신청 - 성공")
    void createRefund_Success() {
        // Given
        given(refundService.createRefund(any(RefundRequestDto.class), any(User.class)))
                .willReturn(refundResponseDto);

        // When
        var result = refundController.createRefund(refundRequestDto, testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().refundId()).isEqualTo(1L);
        assertThat(result.getBody().orderId()).isEqualTo(1L);
        assertThat(result.getBody().orderNumber()).isEqualTo("ORD123456789");
        assertThat(result.getBody().reason()).isEqualTo("단순변심");
        assertThat(result.getBody().refundAmount()).isEqualTo(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("환불 목록 조회 - 성공")
    void getRefundList_Success() {
        // Given
        List<RefundResponseDto> refunds = List.of(refundResponseDto);
        Page<RefundResponseDto> refundPage = new PageImpl<>(refunds, PageRequest.of(0, 10), 1);
        
        given(refundService.getItemsByUser(any(User.class), any(Pageable.class)))
                .willReturn(refundPage);

        // When
        var result = refundController.getRefundList(PageRequest.of(0, 10), testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
        assertThat(result.getBody().getContent().get(0).refundId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("환불 상세 조회 - 성공")
    void getRefundDetail_Success() {
        // Given
        given(refundService.getItem(eq(1L), any(User.class)))
                .willReturn(refundResponseDto);

        // When
        var result = refundController.getRefundDetail(1L, testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().refundId()).isEqualTo(1L);
        assertThat(result.getBody().orderNumber()).isEqualTo("ORD123456789");
    }

    @Test
    @DisplayName("환불 상세 조회 - 존재하지 않는 환불")
    void getRefundDetail_NotFound() {
        // Given
        given(refundService.getItem(eq(999L), any(User.class)))
                .willThrow(new IllegalArgumentException("환불 정보를 찾을 수 없습니다."));

        // When & Then
        assertThatThrownBy(() -> refundController.getRefundDetail(999L, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환불 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("환불 승인 - 성공")
    void approveRefund_Success() {
        // Given
        RefundResponseDto approvedResponseDto = new RefundResponseDto(
                1L, 1L, "ORD123456789", Refund.RefundStatus.COMPLETED,
                "단순변심", "색상이 마음에 들지 않음", BigDecimal.valueOf(50000),
                Refund.RefundMethod.BANK_TRANSFER, List.of(),
                LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        
        given(refundService.approveItem(eq(1L), any(User.class)))
                .willReturn(approvedResponseDto);

        // When
        var result = refundController.approveRefund(1L, testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().refundId()).isEqualTo(1L);
        assertThat(result.getBody().status()).isEqualTo(Refund.RefundStatus.COMPLETED);
    }
}