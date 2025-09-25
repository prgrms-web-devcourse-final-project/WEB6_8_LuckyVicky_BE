
package com.back.domain.product.product.controller;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.CreateProductRequest;
import com.back.domain.product.product.entity.ProductImage;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3FileRequest;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
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

    // SpyBean으로 변경: 실제 객체를 사용하지만, 원하는 메서드만 가짜로 바꿀 수 있음
    @SpyBean
    private ProductService productService;

    @MockBean
    private S3Service s3Service;

    // SpyBean으로 인해 실제 ProductService가 사용되므로, 그 의존성을 Mock으로 만듦
    @MockBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("상품 등록 API 성공")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void createProduct_test() throws Exception {
        // given
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

        // Spy 객체의 메서드를 가짜로 만들 때는 doReturn-when 구문 사용
        doReturn(1L).when(productService).createProduct(any(), any());

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("상품 등록 실패 - 유효성 검사 실패 (상품명 누락)")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void createProduct_Fail_With_Invalid_Input() throws Exception {
        // given
        CreateProductRequest request = new CreateProductRequest(
                1L, "", "테스트 브랜드", 10000, 10, // name is blank
                true, 3000, 3000, "PAID", null,
                100, "상세 설명", "SELLING", "DISPLAYED",
                1, 10, false, false, null, null,
                List.of(1L, 2L), null, null, List.of(),
                "테스트 모델", true, "한국", "면", "L"
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest()) // HTTP 400 응답 예상
                .andExpect(content().string(containsString("상품명은 필수입니다.")));
    }

    @Test
    @DisplayName("상품 등록 실패 - 권한 없음 (일반 USER)")
    @WithMockUser(username = "user@test.com", roles = "USER") // ARTIST가 아닌 USER 권한
    void createProduct_Fail_With_Invalid_Role() throws Exception {
        // given
        // 실제 ProductService 로직 초반의 Category 조회를 통과시키기 위해 Mock 응답 설정
        given(categoryRepository.findById(any())).willReturn(Optional.of(new Category()));

        // 유효성 검사를 통과하도록 이미지 리스트를 포함
        List<S3FileRequest> images = List.of(
                new S3FileRequest("https://example.com/image.jpg", FileType.MAIN, "product-images/image.jpg", "image.jpg")
        );

        // 요청 데이터는 유효함
        CreateProductRequest request = new CreateProductRequest(
                1L, "테스트 상품", "테스트 브랜드", 10000, 10,
                true, 3000, 3000, "PAID", null,
                100, "상세 설명", "SELLING", "DISPLAYED",
                1, 10, false, false, null, null,
                List.of(1L, 2L), null, null, images,
                "테스트 모델", true, "한국", "면", "L"
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // then
        // 실제 서비스의 권한 체크 if문에서 IllegalStateException이 발생하고,
        // GlobalExceptionHandler가 이를 500 에러로 처리할 것을 기대
        resultActions
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("상품 이미지 업로드 API 테스트")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void uploadProductImages_test() throws Exception {
        // given
        MockMultipartFile file1 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1 content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "image2.png", MediaType.IMAGE_PNG_VALUE, "image2 content".getBytes());

        List<UploadResultResponse> mockResponse = List.of(
                new UploadResultResponse("https://example.com/image1.jpg", FileType.MAIN, "product-images/image1.jpg", "image1.jpg"),
                new UploadResultResponse("https://example.com/thumb_image1.jpg", FileType.THUMBNAIL, "product-images/thumb_image1.jpg", "thumb_image1.jpg"),
                new UploadResultResponse("https://example.com/image2.png", FileType.ADDITIONAL, "product-images/image2.png", "image2.png")
        );
        given(s3Service.uploadFiles(any(), anyString(), any())).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/products/images?types=MAIN&types=ADDITIONAL")
                        .file(file1)
                        .file(file2)
                        .characterEncoding(StandardCharsets.UTF_8)
        ).andDo(print());

        // then
        MvcResult mvcResult = resultActions
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        RsData<List<UploadResultResponse>> rsData = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        assertThat(rsData.resultCode()).isEqualTo("200");
        assertThat(rsData.data()).hasSize(3);
        assertThat(rsData.data().get(0).type()).isEqualTo(FileType.MAIN);
    }

    @Test
    @DisplayName("상품 문서 다운로드 API 테스트")
    @WithMockUser(username = "artist@test.com", roles = "ARTIST")
    void downloadProductDocument_test() throws Exception {
        // given
        ProductImage document = ProductImage.builder()
                .s3Key("product-docs/document.pdf")
                .originalFilename("테스트문서.pdf")
                .build();

        byte[] fileContent = "This is a test document.".getBytes(StandardCharsets.UTF_8);

        // Spy 객체의 메서드를 가짜로 만들 때는 doReturn-when 구문 사용
        doReturn(document).when(productService).getProductDocument(1L);
        given(s3Service.downloadFile(document.getS3Key())).willReturn(fileContent);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/products/images/download/{productId}", 1L)
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"테스트문서.pdf\""))
                .andExpect(content().bytes(fileContent));
    }
}
