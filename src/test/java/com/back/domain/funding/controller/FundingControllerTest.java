package com.back.domain.funding.controller;

import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.funding.service.FundingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("펀딩 컨트롤러 테스트")
public class FundingControllerTest {
    @Autowired
    private FundingRepository fundingRepository;
    @Autowired
    private FundingService fundingService;
    @Autowired
    private MockMvc mvc;

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("1. 펀딩 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                  "description": "테스트 펀딩 설명",
                                  "imageUrl": "http://example.com/image.jpg",
                                  "targetAmount": 1000000,
                                  "startDate": "2025-10-01 00:00:00",
                                  "endDate": "2025-12-31 23:59:59",
                                  "options": [
                                    {
                                      "name": "옵션 1",
                                      "price": 50000,
                                      "stock": 100,
                                      "sortOrder": 1
                                    },
                                    {
                                      "name": "옵션 2",
                                      "price": 100000,
                                      "stock": 50,
                                      "sortOrder": 2
                                    }
                                  ]
                                }
                                """))
                .andDo(print());

        // then (응답 JSON 전체 검증)
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("테스트 펀딩"))
                .andExpect(jsonPath("$.data.description").value("테스트 펀딩 설명"))
                .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.data.targetAmount").value(1000000))
                .andExpect(jsonPath("$.data.startDate").value("2025-10-01 00:00:00"))
                .andExpect(jsonPath("$.data.endDate").value("2025-12-31 23:59:59"))
                .andExpect(jsonPath("$.data.options[0].name").value("옵션 1"))
                .andExpect(jsonPath("$.data.options[0].price").value(50000))
                .andExpect(jsonPath("$.data.options[0].stock").value(100))
                .andExpect(jsonPath("$.data.options[0].sortOrder").value(1))
                .andExpect(jsonPath("$.data.options[1].name").value("옵션 2"))
                .andExpect(jsonPath("$.data.options[1].price").value(100000))
                .andExpect(jsonPath("$.data.options[1].stock").value(50))
                .andExpect(jsonPath("$.data.options[1].sortOrder").value(2));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("2. 펀딩 생성 실패 - 권한 없음")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                  "description": "테스트 펀딩 설명",
                                  "imageUrl": "http://example.com/image.jpg",
                                  "targetAmount": 1000000,
                                  "startDate": "2025-10-01 00:00:00",
                                  "endDate": "2025-12-31 23:59:59",
                                  "options": [
                                    {
                                      "name": "옵션 1",
                                      "price": 50000,
                                      "stock": 100,
                                      "sortOrder": 1
                                    },
                                    {
                                      "name": "옵션 2",
                                      "price": 100000,
                                      "stock": 50,
                                      "sortOrder": 2
                                    }
                                  ]
                                }
                                """))
                .andDo(print());

        // then (권한 없음)
        resultActions.andExpect(status().isInternalServerError());

    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("3. 펀딩 생성 실패 - 제목 누락")
    void t3() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "테스트 펀딩 설명",
                                  "imageUrl": "http://example.com/image.jpg",
                                  "targetAmount": 1000000,
                                  "startDate": "2025-10-01 00:00:00",
                                  "endDate": "2025-12-31 23:59:59",
                                  "options": [
                                    {
                                      "name": "옵션 1",
                                      "price": 50000,
                                      "stock": 100,
                                      "sortOrder": 1
                                    },
                                    {
                                      "name": "옵션 2",
                                      "price": 100000,
                                      "stock": 50,
                                      "sortOrder": 2
                                    }
                                  ]
                                }
                                """))
                .andDo(print());

        // then (필수 입력값 누락 - title)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("title: 펀딩 제목은 필수입니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("4. 펀딩 생성 실패 - 옵션 누락")
    void t4() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                  "description": "테스트 펀딩 설명",
                                  "imageUrl": "http://example.com/image.jpg",
                                  "targetAmount": 1000000,
                                  "startDate": "2025-10-01 00:00:00",
                                  "endDate": "2025-12-31 23:59:59"
                                }
                                """))
                .andDo(print());

        // then (필수 입력값 누락 - options)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("options: 펀딩 옵션은 최소 1개 이상 필요합니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("5. 펀딩 생성 실패 - 옵션 이름 누락")
    void t5() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                    "description": "테스트 펀딩 설명",
                                    "imageUrl": "http://example.com/image.jpg",
                                    "targetAmount": 1000000,
                                    "startDate": "2025-10-01 00:00:00",
                                    "endDate": "2025-12-31 23:59:59",
                                    "options": [
                                        {
                                            "price": 50000,
                                            "stock": 100,
                                            "sortOrder": 1
                                            },
                                            {
                                            "name": "옵션 2",
                                            "price": 100000,
                                            "stock": 50,
                                            "sortOrder": 2
                                        }
                                    ]
                                }
                                """))
                .andDo(print());

        // then (필수 입력값 누락 - options.name)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("옵션명은 필수입니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("6. 펀딩 생성 실패 - 목표 금액 0원 이하")
    void t6() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                    "description": "테스트 펀딩 설명",
                                    "imageUrl": "http://example.com/image.jpg",
                                    "targetAmount": 0,
                                    "startDate": "2025-10-01 00:00:00",
                                    "endDate": "2025-12-31 23:59:59",
                                    "options": [
                                        {
                                            "name": "옵션 1",
                                            "price": 50000,
                                            "stock": 100,
                                            "sortOrder": 1
                                            },
                                            {
                                            "name": "옵션 2",
                                            "price": 100000,
                                            "stock": 50,
                                            "sortOrder": 2
                                        }
                                    ]
                                }
                                """))
                .andDo(print());

        // then (목표 금액 0원 이하)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("targetAmount: 목표 금액은 0보다 커야 합니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("7. 펀딩 생성 실패 - 종료일이 시작일 이전")
    void t7() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                    "description": "테스트 펀딩 설명",
                                    "imageUrl": "http://example.com/image.jpg",
                                    "targetAmount": 1000000,
                                    "startDate": "2025-12-31 23:59:59",
                                    "endDate": "2025-10-01 00:00:00",
                                    "options": [
                                        {
                                            "name": "옵션 1",
                                            "price": 50000,
                                            "stock": 100,
                                            "sortOrder": 1
                                            },
                                            {
                                            "name": "옵션 2",
                                            "price": 100000,
                                            "stock": 50,
                                            "sortOrder": 2
                                        }
                                    ]
                                }
                                """))
                .andDo(print());

        // then (종료일이 시작일 이전)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("종료일은 시작일 이후여야 합니다."));
    }

    @Test
    @DisplayName("8. 펀딩 조회")
    void t8() throws Exception {
        Long id = 1L;

        ResultActions resultActions = mvc.perform(get("/api/fundings/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("펀딩 1 입니다."))
                .andExpect(jsonPath("$.data.description").value("펀딩 1이요~~"))
                .andExpect(jsonPath("$.data.imageUrl").value("www.example.com"))
                .andExpect(jsonPath("$.data.targetAmount").value(1_000_000))
                .andExpect(jsonPath("$.data.startDate").exists())
                .andExpect(jsonPath("$.data.endDate").exists())
                // 옵션 배열 검증
                .andExpect(jsonPath("$.data.options.length()").value(2))
                .andExpect(jsonPath("$.data.options[0].name").value("1 옵션이요~"))
                .andExpect(jsonPath("$.data.options[0].price").value(10_000))
                .andExpect(jsonPath("$.data.options[0].stock").value(11110))
                .andExpect(jsonPath("$.data.options[1].name").value("2 옵션이요~"))
                .andExpect(jsonPath("$.data.options[1].price").value(15_000))
                .andExpect(jsonPath("$.data.options[1].stock").value(11110));
    }

    @Test
    @DisplayName("GET /api/fundings - 전체 조회")
    void getFundings_All() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.totalElements").value(4))
                .andExpect(jsonPath("$.data.content[0].title").exists())
                .andExpect(jsonPath("$.data.content[0].authorName").value("유저1"));
    }

    @Test
    @DisplayName("GET /api/fundings?keyword=앨범 - 키워드 검색")
    void getFundings_SearchByKeyword() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "앨범"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value(containsString("앨범")));
    }

    @Test
    @DisplayName("GET /api/fundings?keyword=포토북 - 포토북 검색")
    void getFundings_SearchPhotobook() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "포토북"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value(containsString("포토북")));
    }

    @Test
    @DisplayName("GET /api/fundings?statuses=OPEN - 상태 필터")
    void getFundings_FilterByStatus() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("statuses", "OPEN"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings?minPrice=30000 - 최소 가격 필터")
    void getFundings_FilterByMinPrice() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("minPrice", "30000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2))); // 35000, 80000원 옵션
    }

    @Test
    @DisplayName("GET /api/fundings?maxPrice=20000 - 최대 가격 필터")
    void getFundings_FilterByMaxPrice() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("maxPrice", "20000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2))); // 15000원 옵션
    }

    @Test
    @DisplayName("GET /api/fundings?minPrice=10000&maxPrice=50000 - 가격 범위")
    void getFundings_FilterByPriceRange() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("minPrice", "10000")
                        .param("maxPrice", "50000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3))); // 15000, 35000원 옵션
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=popular - 인기순 정렬")
    void getFundings_SortByPopular() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "popular"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].title").value("신규 앨범 제작")); // 참여자 50명
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=deadline - 마감임박순")
    void getFundings_SortByDeadline() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "deadline"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].title").value("펀딩 1 입니다.")); // 5일 남음
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=highAmount - 목표금액 높은순")
    void getFundings_SortByHighAmount() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "highAmount"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].title").value("프리미엄 굿즈")) // 200만원
                .andExpect(jsonPath("$.data.content[0].targetAmount").value(2000000));
    }

    @Test
    @DisplayName("GET /api/fundings?page=0&size=2 - 페이징")
    void getFundings_Paging() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(4))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(2));
    }

    @Test
    @DisplayName("GET /api/fundings - 복합 필터 (상태+키워드+가격)")
    void getFundings_MultipleFilters() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("statuses", "OPEN")
                        .param("keyword", "앨범")
                        .param("minPrice", "10000")
                        .param("maxPrice", "20000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("신규 앨범 제작"));
    }

    @Test
    @DisplayName("GET /api/fundings?keyword=없는검색어 - 검색 결과 없음")
    void getFundings_NotFound() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "존재하지않는검색어"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/fundings - 응답 DTO 필드 검증")
    void getFundings_ResponseFields() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].title").exists())
                .andExpect(jsonPath("$.data.content[0].imageUrl").exists())
                .andExpect(jsonPath("$.data.content[0].authorName").exists())
                .andExpect(jsonPath("$.data.content[0].targetAmount").exists())
                .andExpect(jsonPath("$.data.content[0].currentAmount").exists())
                .andExpect(jsonPath("$.data.content[0].progress").exists())
                .andExpect(jsonPath("$.data.content[0].remainingDays").exists());
    }

    @Test
    @DisplayName("GET /api/fundings - Page 메타데이터 검증")
    void getFundings_PageMetadata() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.size").exists())
                .andExpect(jsonPath("$.data.number").exists())
                .andExpect(jsonPath("$.data.first").exists())
                .andExpect(jsonPath("$.data.last").exists());
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=recent - 최신순 정렬 (기본값)")
    void getFundings_SortByRecent() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings - sortBy 파라미터 없을 때 (기본값 적용)")
    void getFundings_DefaultSort() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings?statuses=OPEN,CLOSED - 여러 상태 필터")
    void getFundings_MultipleStatuses() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("statuses", "OPEN,CLOSED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings?page=1 - 2페이지 조회")
    void getFundings_SecondPage() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    @DisplayName("GET /api/fundings?minPrice=100000 - 범위 벗어난 가격")
    void getFundings_PriceOutOfRange() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("minPrice", "100000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/fundings - URL 인코딩된 한글 키워드")
    void getFundings_EncodedKeyword() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "앨범"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value(containsString("앨범")));
    }

    @Test
    @DisplayName("GET /api/fundings - 진행률 계산 검증")
    void getFundings_ProgressCalculation() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "앨범"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].progress").isNumber())
                .andExpect(jsonPath("$.data.content[0].progress").value(greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("GET /api/fundings - 남은 일수 계산 검증")
    void getFundings_RemainingDaysCalculation() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].remainingDays").isNumber());
    }
}
