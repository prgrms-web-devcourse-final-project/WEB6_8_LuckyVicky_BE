package com.back.domain.product.product.controller;

import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.entity.DisplayStatus;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.product.tag.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("MainProductController 통합 테스트 - 전체 상품 조회 기준")
public class MainProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    private User artistUser;
    private Category category;

    @BeforeEach
    void setUp() {
        artistUser = userRepository.findByEmail("artist1@artist.com").orElseThrow();
        category = categoryRepository.findByCategoryName("도자기").orElseThrow();
    }

    @Test
    @DisplayName("메인페이지에서 신상품 조회 - 최신순 정렬, DisplayStatus DISPLAYING인 것만 조회")
    void getNewProducts_Success() throws Exception {
        // 최근 14일 이내 상품 생성
        createProduct("상품1", LocalDateTime.now().minusDays(10));
        createProduct("상품2", LocalDateTime.now().minusDays(5));
        createProduct("상품3", LocalDateTime.now().minusDays(1));
        createProduct("상품4", LocalDateTime.now().minusDays(3), DisplayStatus.BEFORE_DISPLAY); // 조회 제외

        // API 호출
        ResultActions resultActions = mockMvc.perform(
                get("/api/products/new")
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3))) // DISPLAYING 3개만 반환
                .andExpect(jsonPath("$.data[0].name").value("상품3")) // 최신순
                .andExpect(jsonPath("$.data[1].name").value("상품2"))
                .andExpect(jsonPath("$.data[2].name").value("상품1"));
    }

    private Product createProduct(String name, LocalDateTime createDate) throws Exception {
        return createProduct(name, createDate, DisplayStatus.DISPLAYING);
    }

    private Product createProduct(String name, LocalDateTime createDate, DisplayStatus status) throws Exception {
        Product product = Product.builder()
                .user(artistUser)
                .category(category)
                .name(name)
                .brandName("브랜드")
                .price(10000)
                .discountRate(0)
                .bundleShippingAvailable(false)
                .deliveryCharge(0)
                .additionalShippingCharge(0)
                .deliveryType(com.back.domain.product.product.entity.DeliveryType.FREE)
                .stock(0)
                .description("테스트용 설명")
                .displayStatus(status)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("테스트 모델")
                .certification(false)
                .origin("대한민국")
                .material("면")
                .size("FREE")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();

        // 리플렉션으로 createDate 세팅
        Field field = product.getClass().getSuperclass().getDeclaredField("createDate");
        field.setAccessible(true);
        field.set(product, createDate);

        return productRepository.save(product);
    }
}
