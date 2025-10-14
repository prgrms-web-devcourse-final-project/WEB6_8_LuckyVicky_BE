package com.back.domain.product.qna.controller;

import com.back.domain.product.qna.dto.request.ProductQnaRequestDto;
import com.back.domain.product.qna.dto.response.ProductQnaListResponseDto;
import com.back.domain.product.qna.dto.response.ProductQnaResponseDto;
import com.back.domain.product.qna.service.ProductQnaService;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import com.back.global.security.auth.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("상품 Q&A ProductQnaController 통합 테스트")
public class ProductQnaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductQnaService productQnaService;

    @MockBean
    private S3Service s3Service;

    private CustomUserDetails createTestUserDetails() {
        User mockUser = mock(User.class);

        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.getPassword()).thenReturn("password");
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getRole()).thenReturn(Role.USER);

        return new CustomUserDetails(
                mockUser,
                Role.USER
        );
    }

    @Test
    @DisplayName("상품 Q&A 등록 성공")
    void createProductQna_success() throws Exception {
        // Given
        UUID productUuid = UUID.randomUUID();
        ProductQnaRequestDto requestDto = new ProductQnaRequestDto(
                "배송",
                "배송 문의합니다.",
                "상품 배송이 언제쯤 시작될까요?",
                Collections.emptyList()
        );
        UUID createdQnaUuid = UUID.randomUUID();

        when(productQnaService.createProductQna(eq(productUuid), any(ProductQnaRequestDto.class), any(CustomUserDetails.class)))
                .thenReturn(createdQnaUuid);

        // When & Then
        mockMvc.perform(post("/api/products/qna/{productUuid}", productUuid)
                        .with(csrf())
                        .with(user(createTestUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("상품 Q&A가 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data").value(createdQnaUuid.toString()));
    }

    @Test
    @DisplayName("상품 Q&A 상세 조회 성공")
    void getProductQnaDetail_success() throws Exception {
        // Given
        UUID productUuid = UUID.randomUUID();
        Long qnaId = 1L;
        ProductQnaResponseDto responseDto = new ProductQnaResponseDto(
                qnaId,
                "배송",
                "배송 문의합니다.",
                "상품 배송이 언제쯤 시작될까요?",
                "Test User",
                "23.10.26",
                List.of(new UploadResultResponse("http://example.com/image.jpg", null, "s3key", "image.jpg"))
        );

        when(productQnaService.getProductQnaDetail(eq(qnaId)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/products/qna/{productUuid}/{productQnaId}", productUuid, qnaId)
                        .with(user(createTestUserDetails())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("상품 Q&A 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(qnaId))
                .andExpect(jsonPath("$.data.qnaCategory").value("배송"))
                .andExpect(jsonPath("$.data.qnaTitle").value("배송 문의합니다."))
                .andExpect(jsonPath("$.data.qnaDescription").value("상품 배송이 언제쯤 시작될까요?"))
                .andExpect(jsonPath("$.data.authorName").value("Test User"))
                .andExpect(jsonPath("$.data.createDate").value("23.10.26"))
                .andExpect(jsonPath("$.data.qnaImages[0].url").value("http://example.com/image.jpg"));
    }

    @Test
    @DisplayName("상품 Q&A 목록 조회 성공 (페이지네이션)")
    void getProductQnaList_success() throws Exception {
        // Given
        UUID productUuid = UUID.randomUUID();
        int page = 1;
        int size = 10;

        ProductQnaResponseDto qna1 = new ProductQnaResponseDto(
                1L, "배송", "배송 문의1", "내용1", "User1", "23.10.26", Collections.emptyList());
        ProductQnaResponseDto qna2 = new ProductQnaResponseDto(
                2L, "상품", "상품 문의2", "내용2", "User2", "23.10.25", Collections.emptyList());

        ProductQnaListResponseDto listResponseDto = new ProductQnaListResponseDto(
                page,
                1,
                size,
                2L,
                List.of(qna1, qna2)
        );

        when(productQnaService.getProductQnaList(eq(productUuid), eq(page), eq(size)))
                .thenReturn(listResponseDto);

        // When & Then
        mockMvc.perform(get("/api/products/qna/{productUuid}/list", productUuid)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .with(user(createTestUserDetails())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("상품 Q&A 목록 조회 성공"))
                .andExpect(jsonPath("$.data.currentPage").value(page))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(size))
                .andExpect(jsonPath("$.data.totalElements").value(2L))
                .andExpect(jsonPath("$.data.qnaList[0].id").value(1L))
                .andExpect(jsonPath("$.data.qnaList[0].qnaTitle").value("배송 문의1"))
                .andExpect(jsonPath("$.data.qnaList[1].id").value(2L))
                .andExpect(jsonPath("$.data.qnaList[1].qnaTitle").value("상품 문의2"));
    }
}