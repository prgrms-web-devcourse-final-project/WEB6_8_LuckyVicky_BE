package com.back.domain.order.exchange.controller;

import com.back.domain.order.exchange.dto.request.ExchangeRequestDto;
import com.back.domain.order.exchange.dto.response.ExchangeResponseDto;
import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeReasonType;
import com.back.domain.order.exchange.service.ExchangeService;
import com.back.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ExchangeControllerTest {

    private ExchangeController exchangeController;
    private ExchangeService exchangeService;
    private ObjectMapper objectMapper;

    private User testUser;
    private ExchangeRequestDto exchangeRequestDto;
    private ExchangeResponseDto exchangeResponseDto;

    @BeforeEach
    void setUp() throws Exception {
        // Mock 서비스 설정
        exchangeService = mock(ExchangeService.class);
        objectMapper = new ObjectMapper();
        exchangeController = new ExchangeController(exchangeService);

        // 테스트 사용자 설정 (reflection으로 ID 설정)
        testUser = User.createLocalUser("test@example.com", "password123", "테스트사용자", "010-1234-5678");
        java.lang.reflect.Field idField = testUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, 1L);

        // 교환 요청 DTO
        exchangeRequestDto = new ExchangeRequestDto(
                1L, // orderId
                List.of(1L, 2L), // orderItemIds
                ExchangeReasonType.CHANGE_OF_MIND, // reasonType
                "색상 불일치", // reason
                "주문한 색상과 다른 색상이 배송됨", // detailReason
                Exchange.ExchangeMethod.PICKUP, // exchangeMethod
                List.of("image1.jpg", "image2.jpg"), // attachmentFiles
                "서울시 강남구", // newShippingAddress1
                "테헤란로 123", // newShippingAddress2
                "12345", // newShippingZip
                "홍길동", // newRecipientName
                "010-9876-5432" // newRecipientPhone
        );

        // 교환 응답 DTO
        exchangeResponseDto = new ExchangeResponseDto(
                1L, // exchangeId
                1L, // orderId
                "ORD123456789", // orderNumber
                Exchange.ExchangeStatus.REQUESTED, // status
                "색상 불일치", // reason
                "주문한 색상과 다른 색상이 배송됨", // detailReason
                Exchange.ExchangeMethod.PICKUP, // exchangeMethod
                List.of("image1.jpg", "image2.jpg"), // attachmentFiles
                "서울시 강남구", // newShippingAddress1
                "테헤란로 123", // newShippingAddress2
                "12345", // newShippingZip
                "홍길동", // newRecipientName
                "010-9876-5432", // newRecipientPhone
                LocalDateTime.now(), // createdAt
                LocalDateTime.now(), // updatedAt
                List.of( // exchangeItems
                        new ExchangeResponseDto.ExchangeItemResponseDto(
                                1L, // exchangeItemId
                                1L, // orderItemId
                                "테스트 상품", // productName
                                "http://example.com/image.jpg", // productThumbnailUrl
                                2, // quantity
                                "빨강, L" // optionInfo
                        )
                )
        );
    }

    @Test
    @DisplayName("교환 신청 - 성공")
    void createExchange_Success() {
        // Given
        given(exchangeService.createExchange(any(ExchangeRequestDto.class), any(User.class)))
                .willReturn(exchangeResponseDto);

        // When
        var result = exchangeController.createExchange(exchangeRequestDto, testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().exchangeId()).isEqualTo(1L);
        assertThat(result.getBody().orderId()).isEqualTo(1L);
        assertThat(result.getBody().orderNumber()).isEqualTo("ORD123456789");
        assertThat(result.getBody().reason()).isEqualTo("색상 불일치");
        assertThat(result.getBody().exchangeMethod()).isEqualTo(Exchange.ExchangeMethod.PICKUP);
        assertThat(result.getBody().newShippingAddress1()).isEqualTo("서울시 강남구");
        assertThat(result.getBody().newRecipientName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("교환 목록 조회 - 성공")
    void getExchangeList_Success() {
        // Given
        List<ExchangeResponseDto> exchanges = List.of(exchangeResponseDto);
        Page<ExchangeResponseDto> exchangePage = new PageImpl<>(exchanges, PageRequest.of(0, 10), 1);
        
        given(exchangeService.getItemsByUser(any(User.class), any(Pageable.class)))
                .willReturn(exchangePage);

        // When
        var result = exchangeController.getExchangeList(PageRequest.of(0, 10), testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
        assertThat(result.getBody().getContent().get(0).exchangeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("교환 상세 조회 - 성공")
    void getExchangeDetail_Success() {
        // Given
        given(exchangeService.getItem(eq(1L), any(User.class)))
                .willReturn(exchangeResponseDto);

        // When
        var result = exchangeController.getExchangeDetail(1L, testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().exchangeId()).isEqualTo(1L);
        assertThat(result.getBody().orderNumber()).isEqualTo("ORD123456789");
    }

    @Test
    @DisplayName("교환 상세 조회 - 존재하지 않는 교환")
    void getExchangeDetail_NotFound() {
        // Given
        given(exchangeService.getItem(eq(999L), any(User.class)))
                .willThrow(new IllegalArgumentException("교환 정보를 찾을 수 없습니다."));

        // When & Then
        assertThatThrownBy(() -> exchangeController.getExchangeDetail(999L, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("교환 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("교환 승인 - 성공")
    void approveExchange_Success() {
        // Given
        ExchangeResponseDto approvedResponseDto = new ExchangeResponseDto(
                1L, // exchangeId
                1L, // orderId
                "ORD123456789", // orderNumber
                Exchange.ExchangeStatus.COMPLETED, // status
                "색상 불일치", // reason
                "주문한 색상과 다른 색상이 배송됨", // detailReason
                Exchange.ExchangeMethod.PICKUP, // exchangeMethod
                List.of(), // attachmentFiles
                "서울시 강남구", // newShippingAddress1
                "테헤란로 123", // newShippingAddress2
                "12345", // newShippingZip
                "홍길동", // newRecipientName
                "010-9876-5432", // newRecipientPhone
                LocalDateTime.now(), // createdAt
                LocalDateTime.now(), // updatedAt
                List.of() // exchangeItems
        );
        
        given(exchangeService.approveItem(eq(1L), any(User.class)))
                .willReturn(approvedResponseDto);

        // When
        var result = exchangeController.approveExchange(1L, testUser);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().exchangeId()).isEqualTo(1L);
        assertThat(result.getBody().status()).isEqualTo(Exchange.ExchangeStatus.COMPLETED);
    }
}