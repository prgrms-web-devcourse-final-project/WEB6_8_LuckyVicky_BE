package com.back.domain.product.product.controller;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.dto.request.CreateProductRequest;
import com.back.domain.product.product.dto.request.UpdateProductRequest;
import com.back.domain.product.product.entity.*;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.product.tag.entity.Tag;
import com.back.domain.product.tag.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.s3.*;
import com.back.global.security.auth.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("ProductController 통합 테스트")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private ArtistApplicationRepository artistApplicationRepository;
    @Autowired
    private EntityManager em;

    @MockBean
    private S3Service s3Service;
    @MockBean
    private S3ValidationService s3ValidationService;

    private User artistUser;
    private User normalUser;
    private Category category;
    private Tag tag1, tag2;

    @BeforeEach
    void setUp() {
        // S3 Mocking
        given(s3Service.uploadFiles(any(), any(), any())).willReturn(
                List.of(new UploadResultResponse("https://example.com/main.jpg", FileType.MAIN, "s3key1", "main.jpg"),
                        new UploadResultResponse("https://example.com/thumb.jpg", FileType.THUMBNAIL, "s3key2", "thumb.jpg"))
        );
                when(s3ValidationService.validateFileExists(anyString())).thenReturn(true);

        // TestInitData가 생성한 데이터 조회
        artistUser = userRepository.findByEmail("artist1@artist.com").orElseThrow();
        normalUser = userRepository.findByEmail("user2@user.com").orElseThrow();
                category = categoryRepository.findByCategoryName("도자기").orElseThrow();

        tag1 = tagRepository.findByName("태그1").orElseThrow();
        tag2 = tagRepository.findByName("태그2").orElseThrow();
    }

    // ==================== 상품 등록 (Create) ====================

    @Test
    @DisplayName("ARTIST 권한으로 상품 등록에 성공한다")
    void createProduct_Success() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(artistUser, artistUser.getRole());
        CreateProductRequest request = createSampleProductRequest();

        // When
        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("상품이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data").isString());

        String productUuid = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("data").asText();
        Product foundProduct = productRepository.findByProductUuid(UUID.fromString(productUuid)).orElseThrow();

        assertThat(foundProduct.getName()).isEqualTo("테스트 상품");
        assertThat(foundProduct.getUser().getEmail()).isEqualTo(artistUser.getEmail());
        assertThat(foundProduct.getProductTags()).hasSize(2);
    }

    @Test
    @DisplayName("USER 권한으로 상품 등록 시 403 에러가 발생한다")
    void createProduct_Fail_Unauthorized() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(normalUser, normalUser.getRole());
        CreateProductRequest request = createSampleProductRequest();

        // When
        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상품 등록 시 필수 입력값이 누락되면 400 에러가 발생한다")
    void createProduct_Fail_InvalidInput() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(artistUser, artistUser.getRole());
        CreateProductRequest request = new CreateProductRequest(
                category.getId(), "", "브랜드", 10000, 10, true, 3000, 50000, "FREE",
                null, 100, "설명", "SELLING", "DISPLAYING", 1, 10, false, false, null, null,
                List.of(tag1.getId(), tag2.getId()), null, null, createSampleImages(),
                "모델", true, "국산", "면", "L"
        );

        // When
        ResultActions resultActions = mockMvc.perform(
                post("/api/products")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("상품명은 필수입니다.")));
    }

    // ==================== 상품 상세 조회 (Read) ====================

    @Test
    @DisplayName("상품 상세 정보를 성공적으로 조회한다")
    void getProductDetail_Success() throws Exception {
        // Given
        // 상품 상세 조회 시 작가 정보 조회를 위해 ArtistApplication 데이터 필요
                artistApplicationRepository.save(ArtistApplication.builder()
                .user(artistUser)
                .artistName("테스트작가")
                .ownerName("테스트대표")
                .email("test@test.com")
                .phone("010-0000-0000")
                .build());

        Product product = productRepository.save(createSampleProduct(artistUser, category, List.of(tag1, tag2)));
        UUID productUuid = product.getProductUuid();

        // When
        ResultActions resultActions = mockMvc.perform(
                get("/api/products/{productUuid}", productUuid)
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.productUuid").value(productUuid.toString()))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.tags[0].tagName").value("태그1"));
    }

    @Test
    @DisplayName("존재하지 않는 상품 UUID로 조회 시 404 에러가 발생한다")
    void getProductDetail_Fail_NotFound() throws Exception {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();

        // When
        ResultActions resultActions = mockMvc.perform(
                get("/api/products/{productUuid}", nonExistentUuid)
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 상품입니다. UUID: " + nonExistentUuid));
    }

    // ==================== 상품 목록 조회 (Read) ====================

    @Test
    @DisplayName("다양한 조건으로 상품 목록을 필터링하고 정렬하여 조회한다")
    void getProducts_Success_WithFiltersAndSort() throws Exception {
        // Given
        productRepository.save(createSampleProduct(artistUser, category, List.of(tag1), "상품A", 10000));
        productRepository.save(createSampleProduct(artistUser, category, List.of(tag2), "상품B", 20000));
        productRepository.save(createSampleProduct(artistUser, category, List.of(tag1, tag2), "상품C", 30000));

        // When
        ResultActions resultActions = mockMvc.perform(
                get("/api/products")
                        .param("tagIds", tag1.getId().toString())
                        .param("minPrice", "5000")
                        .param("maxPrice", "35000")
                        .param("sort", "priceAsc")
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products", hasSize(2)))
                .andExpect(jsonPath("$.data.products[0].name").value("상품A"))
                .andExpect(jsonPath("$.data.products[1].name").value("상품C"));
    }

    @Test
    @DisplayName("조회 결과가 없는 경우 빈 목록을 반환한다")
    void getProducts_Success_EmptyResult() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(
                get("/api/products")
                        .param("categoryId", "9999") // 존재하지 않는 카테고리
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products", hasSize(0)));
    }

    // ==================== 상품 수정 (Update) ====================

    @Test
    @DisplayName("상품 소유자가 상품 정보를 성공적으로 수정한다")
    void updateProduct_Success() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(artistUser, artistUser.getRole());
        Product product = productRepository.save(createSampleProduct(artistUser, category, List.of(tag1, tag2)));
        UUID productUuid = product.getProductUuid();

        UpdateProductRequest request = new UpdateProductRequest(
                category.getId(), "수정된 상품", "수정된 브랜드", 20000, 20, true, 3000, 50000, "FREE",
                null, 100, "수정된 설명", "SELLING", "DISPLAYING", 1, 10, false, false, null, null,
                List.of(tag1.getId()), null, null, createSampleImages(),
                "모델", true, "국산", "면", "L"
        );

        // When
        ResultActions resultActions = mockMvc.perform(
                put("/api/products/{productUuid}", productUuid)
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(productUuid.toString()));

        // Persistence context를 초기화하여 최신 상태를 로드
        em.clear(); // 이전에 em.flush()가 OptimisticLockException을 발생시켰으므로 제거

        Product updatedProduct = productRepository.findByProductUuid(productUuid).orElseThrow();
        assertThat(updatedProduct.getName()).isEqualTo("수정된 상품");
        assertThat(updatedProduct.getPrice()).isEqualTo(20000);
        assertThat(updatedProduct.getProductTags()).hasSize(1);
    }

    @Test
    @DisplayName("소유자가 아닌 다른 유저가 상품 수정을 시도하면 403 에러가 발생한다")
    void updateProduct_Fail_Forbidden() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(normalUser, normalUser.getRole());
        Product product = productRepository.save(createSampleProduct(artistUser, category, List.of(tag1)));
        UUID productUuid = product.getProductUuid();
        UpdateProductRequest request = new UpdateProductRequest(
                category.getId(), "해킹 시도", "해킹 브랜드", 1, 1, false, 0, 0, "FREE",
                null, 1, "", "", "", 1, 1, false, false, null, null, null, null, null, null,
                "", false, "", "", ""
        );

        // When
        ResultActions resultActions = mockMvc.perform(
                put("/api/products/{productUuid}", productUuid)
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isForbidden());
    }

    // ==================== 상품 삭제 (Delete) ====================

    @Test
    @DisplayName("상품 소유자가 상품을 성공적으로 논리 삭제한다")
    void deleteProduct_Success() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(artistUser, artistUser.getRole());
        Product product = productRepository.save(createSampleProduct(artistUser, category, List.of(tag1)));
        UUID productUuid = product.getProductUuid();
        Long productId = product.getId();

        // When
        ResultActions resultActions = mockMvc.perform(
                delete("/api/products/{productUuid}", productUuid)
                        .with(user(userDetails))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(productUuid.toString()));

        em.flush();
        em.clear();

        Optional<Product> findByUuid = productRepository.findByProductUuid(productUuid);
        assertThat(findByUuid.orElseThrow().isDeleted()).isTrue();
        assertThat(findByUuid.orElseThrow().getDisplayStatus()).isEqualTo(DisplayStatus.END_OF_DISPLAY);
        assertThat(findByUuid.orElseThrow().getSellingStatus()).isEqualTo(SellingStatus.END_OF_SALE);

        Product deletedProduct = productRepository.findById(productId).orElseThrow();
        assertThat(deletedProduct.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 404 에러가 발생한다")
    void deleteProduct_Fail_NotFound() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(artistUser, artistUser.getRole());
        UUID nonExistentUuid = UUID.randomUUID();

        // When
        ResultActions resultActions = mockMvc.perform(
                delete("/api/products/{productUuid}", nonExistentUuid)
                        .with(user(userDetails))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("존재하지 않는 상품입니다. UUID: " + nonExistentUuid));
    }

    // ==================== 이미지 및 파일 관련 테스트 ====================

    @Test
    @DisplayName("상품 이미지 업로드에 성공한다(S3 서비스 Mocking)")
    void uploadProductImages_Success() throws Exception {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(artistUser, artistUser.getRole());
        MockMultipartFile file1 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "image2.png", MediaType.IMAGE_PNG_VALUE, "image2".getBytes());

        // When
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/products/images")
                        .file(file1)
                        .file(file2)
                        .param("types", "MAIN", "THUMBNAIL")
                        .with(user(userDetails))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("이미지 업로드 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].type").value("MAIN"))
                .andExpect(jsonPath("$.data[1].type").value("THUMBNAIL"));
    }


    // ==================== Helper Methods ====================

    private CreateProductRequest createSampleProductRequest() {
        return new CreateProductRequest(
                category.getId(), "테스트 상품", "테스트 브랜드", 10000, 10, true, 3000, 50000, "FREE",
                null, 100, "상세 설명", "SELLING", "DISPLAYING", 1, 10, false, false, null, null,
                List.of(tag1.getId(), tag2.getId()), null, null, createSampleImages(),
                "테스트 모델", true, "국산", "면", "L"
        );
    }

    private List<S3FileRequest> createSampleImages() {
        return List.of(
                new S3FileRequest("https://example.com/image.jpg", FileType.MAIN, "product-images/image.jpg", "image.jpg"),
                new S3FileRequest("https://example.com/thumb.jpg", FileType.THUMBNAIL, "product-images/thumb.jpg", "thumb.jpg")
        );
    }

    private Product createSampleProduct(User artist, Category category, List<Tag> tags) {
        return createSampleProduct(artist, category, tags, "테스트 상품", 10000);
    }

    private Product createSampleProduct(User artist, Category category, List<Tag> tags, String name, int price) {
        Product product = Product.builder()
                .user(artist)
                .category(category)
                .name(name)
                .brandName("테스트 브랜드")
                .price(price)
                .discountRate(10)
                .bundleShippingAvailable(true)
                .deliveryCharge(3000)
                .additionalShippingCharge(5000)
                .deliveryType(DeliveryType.FREE)
                .stock(100)
                .description("상세 설명")
                .sellingStatus(SellingStatus.SELLING)
                .displayStatus(DisplayStatus.DISPLAYING)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("테스트 모델")
                .certification(true)
                .origin("대한민국")
                .material("면")
                .size("L")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();

        tags.forEach(tag -> {
            ProductTagMapping mapping = ProductTagMapping.builder()
                    .product(product)
                    .tag(tag)
                    .build();
            product.getProductTags().add(mapping);
        });
        return product;
    }
}