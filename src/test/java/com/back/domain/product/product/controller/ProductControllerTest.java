package com.back.domain.product.product.controller;

import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.request.CreateProductRequest;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.entity.ProductImage;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3FileRequest;
import com.back.global.s3.S3Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private ProductService productService;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("상품 등록 API 성공")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void createProduct_test() throws Exception {
        List<S3FileRequest> images = List.of(
                new S3FileRequest("https://example.com/image.jpg", FileType.MAIN, "product-images/image.jpg", "image.jpg")
        );

        CreateProductRequest request = new CreateProductRequest(
                1L, "테스트 상품", "테스트 브랜드", 10000, 10,
                true, 3000, 3000, "PAID", null,
                100, "상세 설명", "SELLING", "DISPLAYED",
                1, 10, false, false, null, null,
                List.of(1L, 2L), null, null, images,
                "테스트 모델", true, "한국", "면", "L"
        );

        UUID mockUuid = UUID.randomUUID();
        doReturn(mockUuid).when(productService).createProduct(any(), any());

        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(mockUuid.toString()));
    }

    @Test
    @DisplayName("상품 등록 실패 - 유효성 검사 실패 (상품명 누락)")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void createProduct_Fail_With_Invalid_Input() throws Exception {
        List<S3FileRequest> images = List.of(
                new S3FileRequest("https://example.com/image.jpg", FileType.MAIN, "product-images/image.jpg", "image.jpg")
        );
        CreateProductRequest request = new CreateProductRequest(
                1L, "", "테스트 브랜드", 10000, 10, // name is blank
                true, 3000, 3000, "PAID", null,
                100, "상세 설명", "SELLING", "DISPLAYED",
                1, 10, false, false, null, null,
                List.of(1L, 2L), null, null, images,
                "테스트 모델", true, "한국", "면", "L"
        );

        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("상품명은 필수입니다.")));
    }

    @Test
    @DisplayName("상품 생성 실패 - 권한 없는 사용자")
    @WithUserDetails(value = "user1@user.com") // TestInitData에서 만든 일반 USER
    void createProduct_Fail_With_Unauthorized_Role() throws Exception {
        List<S3FileRequest> images = List.of(
                new S3FileRequest("https://example.com/image.jpg", FileType.MAIN, "product-images/image.jpg", "image.jpg")
        );
        CreateProductRequest request = new CreateProductRequest(
                1L, "테스트 상품", "테스트 브랜드", 10000, 10,
                true, 3000, 3000, "PAID", null,
                100, "상세 설명", "SELLING", "DISPLAYED",
                1, 10, false, false, null, null,
                List.of(1L, 2L), null, null, images,
                "테스트 모델", true, "한국", "면", "L"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상품 문서 다운로드 API 테스트")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void downloadProductDocument_test() throws Exception {
        ProductImage document = ProductImage.builder()
                .s3Key("product-docs/document.pdf")
                .originalFilename("테스트문서.pdf")
                .build();

        byte[] fileContent = "This is a test document.".getBytes(StandardCharsets.UTF_8);

        UUID productUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        doReturn(document).when(productService).getProductDocument(productUuid);
        given(s3Service.downloadFile(document.getS3Key())).willReturn(fileContent);

        ResultActions resultActions = mockMvc.perform(
                get("/api/products/images/download/{productUuid}", productUuid.toString())
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"테스트문서.pdf\""))
                .andExpect(content().bytes(fileContent));
    }

    @Test
    @DisplayName("상품 목록 조회 API 테스트")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getProducts_test() throws Exception {
        ProductListResponse.ProductInfo product1 = new ProductListResponse.ProductInfo(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
                "https://example.com/image1.jpg",
                "브랜드1",
                "상품1",
                10000,
                10,
                9000,
                null
        );

        ProductListResponse.ProductInfo product2 = new ProductListResponse.ProductInfo(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
                "https://example.com/image2.jpg",
                "브랜드2",
                "상품2",
                20000,
                20,
                16000,
                null
        );

        ProductListResponse mockResponse = new ProductListResponse(
                1,
                10,
                2,
                1,
                List.of(product1, product2)
        );

        doReturn(mockResponse).when(productService).getProducts(
                any(), any(), any(), any(), any(), anyString(), any()
        );

        ResultActions resultActions = mockMvc.perform(
                get("/api/products")
                        .param("categoryId", "1")
                        .param("tagIds", "1", "2")
                        .param("minPrice", "5000")
                        .param("maxPrice", "30000")
                        .param("sort", "priceAsc")
                        .param("page", "1")
                        .param("size", "10")
        ).andDo(print());

        MvcResult mvcResult = resultActions
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        RsData<ProductListResponse> rsData = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );

        assertThat(rsData.resultCode()).isEqualTo("200");
        assertThat(rsData.data().products()).hasSize(2);
        assertThat(rsData.data().products().get(0).name()).isEqualTo("상품1");
        assertThat(rsData.data().products().get(1).discountPrice()).isEqualTo(16000);
    }
}
