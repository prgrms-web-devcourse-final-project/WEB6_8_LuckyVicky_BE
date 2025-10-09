package com.back.domain.product.product.controller;

import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.request.CreateProductRequest;
import com.back.domain.product.product.dto.request.UpdateProductRequest;
import com.back.domain.product.product.dto.response.ProductDetailResponse;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.entity.ProductImage;
import com.back.domain.product.product.service.ProductService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3FileRequest;
import com.back.global.s3.S3Service;
import com.back.global.security.auth.CustomUserDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private ProductService productService;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        Mockito.reset(productService);
    }

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
        // Mock 데이터 생성
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

        given(productService.getProducts(
                eq(1L),
                eq(List.of(1L, 2L)),
                eq(5000),
                eq(30000),
                isNull(),
                eq("priceAsc"),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // API 호출 (기존과 동일)
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

        // 결과 검증
        MvcResult mvcResult = resultActions
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8); // 인코딩 명시 권장
        RsData<ProductListResponse> rsData = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );

        assertThat(rsData.data()).isNotNull();
        assertThat(rsData.resultCode()).isEqualTo("200");
        assertThat(rsData.data().products()).hasSize(2);
        assertThat(rsData.data().products().get(0).name()).isEqualTo("상품1");
        assertThat(rsData.data().products().get(1).discountPrice()).isEqualTo(16000);
    }

    @Test
    @DisplayName("상품 수정 API 성공")
    @WithMockUser(username = "artist@test.com", roles = {"ARTIST"})
    void updateProduct_test() throws Exception {
        UUID productUuid = UUID.fromString("04c089ab-b921-4c83-ac4c-a5a67fef035c");

        UpdateProductRequest request = new UpdateProductRequest(
                1L,
                "수정된 상품명",
                "수정된 브랜드",
                15000,
                15,
                true,
                3000,
                3000,
                "PAID",
                null,
                200,
                "수정된 상세 설명",
                "SELLING",
                "DISPLAYING",
                2,
                20,
                false,
                false,
                null,
                null,
                List.of(1L, 2L),
                null,
                null,
                List.of(new S3FileRequest("https://example.com/image-updated.jpg", FileType.MAIN, "product-images/image-updated.jpg", "image-updated.jpg")),
                "수정된 모델명",
                true,
                "한국",
                "면",
                "M"
        );
        doReturn(productUuid)
                .when(productService)
                .updateProduct(
                        any(UUID.class),
                        any(UpdateProductRequest.class),
                        any()
                );

        mockMvc.perform(
                        put("/api/products/{productUuid}", productUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(productUuid.toString()))
                .andExpect(jsonPath("$.msg").value("상품이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("상품 수정 실패 - 최소/최대 수량 검증 실패")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void updateProduct_fail_minMaxQuantity() throws Exception {
        UUID productUuid = UUID.fromString("ec63cc31-2338-4134-8927-250af25e5809");

        UpdateProductRequest request = new UpdateProductRequest(
                1L,
                "상품명",
                "브랜드명",
                15000,
                15,
                true,
                3000,
                3000,
                "PAID",
                null,
                200,
                "상세 설명",
                "SELLING",
                "DISPLAYING",
                10, // minQuantity
                5,  // maxQuantity < minQuantity
                false,
                false,
                null,
                null,
                List.of(1L, 2L),
                null,
                null,
                List.of(new S3FileRequest("https://example.com/image.jpg", FileType.MAIN, "product-images/image.jpg", "image.jpg")),
                "모델명",
                true,
                "한국",
                "면",
                "M"
        );

        given(productService.updateProduct(
                eq(productUuid),
                any(UpdateProductRequest.class),
                any())
        ).willThrow(new ServiceException("400", "최대 구매 수량은 최소 구매 수량보다 작을 수 없습니다."));

        ResultActions resultActions = mockMvc.perform(
                put("/api/products/{productUuid}", productUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400")) // RsData의 resultCode도 400인지 확인
                .andExpect(content().string(containsString("최대 구매 수량은 최소 구매 수량보다 작을 수 없습니다.")));
    }

    @Test
    @DisplayName("상품 삭제 API 성공")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void deleteProduct_test() throws Exception {
        UUID productUuid = UUID.randomUUID();

        // void 메서드 stub
        doNothing()
                .when(productService).deleteProduct(any(UUID.class), any(CustomUserDetails.class));

        ResultActions resultActions = mockMvc.perform(
                delete("/api/products/{productUuid}", productUuid.toString())
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(productUuid.toString()))
                .andExpect(jsonPath("$.msg").value("상품이 삭제되었습니다."));
    }


    @Test
    @DisplayName("상품 삭제 실패 - 권한 없음")
    @WithMockUser(username = "user@test.com", roles = "USER") // 일반 사용자
    void deleteProduct_fail_unauthorized() throws Exception {
        UUID productUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        ResultActions resultActions = mockMvc.perform(
                delete("/api/products/{productUuid}", productUuid.toString())
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden()); // Body 없이 403만 확인
    }


    @Test
    @DisplayName("상품 삭제 실패 - 상품 없음")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void deleteProduct_fail_notFound() throws Exception {
        UUID productUuid = UUID.randomUUID();

        doThrow(new ServiceException("404", "존재하지 않는 상품입니다."))
                .when(productService)
                .deleteProduct(
                        eq(productUuid),
                        any()
                );

        ResultActions resultActions = mockMvc.perform(
                delete("/api/products/{productUuid}", productUuid.toString())
        ).andDo(print());

        // 결과 검증
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(content().string(containsString("존재하지 않는 상품입니다.")));
    }

    @Test
    @DisplayName("상품 상세 조회 API 성공")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getProductDetail_test_success() throws Exception {
        UUID productUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // Mock ProductDetailResponse 생성
        ProductDetailResponse.ProductEssentialInfo essentialInfo =
                new ProductDetailResponse.ProductEssentialInfo(
                        "MM-CHERRY-24",
                        true,
                        "대한민국",
                        "면 100%",
                        "12x30x5cm",
                        "문구브랜드",
                        "123-45-67890",
                        "김작가",
                        "(주)문구문구/010-1234-5678",
                        "example@company.com",
                        "서울특별시 강남구 테헤란로 1길 100 201호",
                        "2023-서울강남-0001"
                );

        ProductDetailResponse.OptionResponse option1 =
                new ProductDetailResponse.OptionResponse("색상-핑크", 50, 0);
        ProductDetailResponse.OptionResponse option2 =
                new ProductDetailResponse.OptionResponse("색상-화이트", 30, 500);

        ProductDetailResponse.AdditionalProductResponse additional =
                new ProductDetailResponse.AdditionalProductResponse("포장지 추가", 100, 1000);

        ProductDetailResponse.ProductImageResponse image1 =
                new ProductDetailResponse.ProductImageResponse(
                        "https://s3.mori-mori.store/images/cherry_keyring_1.jpg", "IMAGE");
        ProductDetailResponse.ProductImageResponse image2 =
                new ProductDetailResponse.ProductImageResponse(
                        "https://s3.mori-mori.store/images/cherry_keyring_2.jpg", "IMAGE");

        ProductDetailResponse productDetail = new ProductDetailResponse(
                productUuid,
                "김작가",
                "문구브랜드",
                "벚꽃 키링",
                4.6,
                42,
                10000,
                10,
                9000,
                true,
                3000,
                "CONDITIONAL_FREE",
                30000,
                5000,
                List.of(option1, option2),
                List.of(additional),
                List.of(image1, image2),
                essentialInfo,
                100,
                "<p>벚꽃 키링 상세정보 및 사용 방법...</p>",
                1,
                5,
                "SELLING",
                "DISPLAYING",
                false,
                true,
                List.of()
        );

        // Mock 서비스 호출
        given(productService.getProductDetail(productUuid)).willReturn(productDetail);

        ResultActions resultActions = mockMvc.perform(
                get("/api/products/{productUuid}", productUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productUuid").value(productUuid.toString()))
                .andExpect(jsonPath("$.data.name").value("벚꽃 키링"))
                .andExpect(jsonPath("$.data.essentialInfo.productModelName").value("MM-CHERRY-24"))
                .andExpect(jsonPath("$.data.options[0].optionName").value("색상-핑크"))
                .andExpect(jsonPath("$.data.images[1].fileUrl").value("https://s3.mori-mori.store/images/cherry_keyring_2.jpg"));
    }

    @Test
    @DisplayName("상품 상세 조회 API 실패 - 상품 없음")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getProductDetail_test_fail_notFound() throws Exception {
        UUID productUuid = UUID.randomUUID();

        // 상품 없음 시 ServiceException 발생하도록 Mock
        doThrow(new ServiceException("404", "존재하지 않는 상품입니다."))
                .when(productService).getProductDetail(productUuid);

        ResultActions resultActions = mockMvc.perform(
                get("/api/products/{productUuid}", productUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(content().string(containsString("존재하지 않는 상품입니다.")));
    }


}
