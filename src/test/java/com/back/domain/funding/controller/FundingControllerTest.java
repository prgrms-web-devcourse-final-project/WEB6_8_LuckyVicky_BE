package com.back.domain.funding.controller;

import com.back.domain.funding.entity.Funding;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @DisplayName("펀딩 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                  "description": "테스트 펀딩 설명",
                                  "categoryId": 1,
                                  "imageUrl": "test.jpg",
                                  "targetAmount": 1000000,
                                  "price": 100000,
                                  "stock": 1000,
                                  "startDate": "2025-11-20 00:00:00",
                                  "endDate": "2025-12-31 23:59:59"
                                }
                                """))
                .andDo(print());

        // then (응답 JSON 전체 검증)
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("테스트 펀딩"))
                .andExpect(jsonPath("$.data.description").value("테스트 펀딩 설명"))
                .andExpect(jsonPath("$.data.categoryName").value("테스트 카테고리"))
                .andExpect(jsonPath("$.data.targetAmount").value(1000000))
                .andExpect(jsonPath("$.data.startDate").value("2025-11-20 00:00:00"))
                .andExpect(jsonPath("$.data.endDate").value("2025-12-31 23:59:59"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("펀딩 생성 실패 - 권한 없음")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                  "description": "테스트 펀딩 설명",
                                    "categoryId": 1,
                                  "imageUrl": "http://example.com/image.jpg",
                                  "targetAmount": 1000000,
                                  "price": 100000,
                                  "stock": 1000,
                                  "startDate": "2025-10-01 00:00:00",
                                  "endDate": "2025-12-31 23:59:59"
                                }
                                """))
                .andDo(print());

        // then (권한 없음)
        resultActions.andExpect(status().isInternalServerError());

    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 생성 실패 - 제목 누락")
    void t3() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "테스트 펀딩 설명",
                                  "categoryId": 1,
                                  "imageUrl": "http://example.com/image.jpg",
                                  "targetAmount": 1000000,
                                  "price": 100000,
                                  "stock": 1000,
                                  "startDate": "2025-10-01 00:00:00",
                                  "endDate": "2025-12-31 23:59:59"
                                }
                                """))
                .andDo(print());

        // then (필수 입력값 누락 - title)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("title: 펀딩 제목은 필수입니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 생성 실패 - 목표 금액 0원 이하")
    void t6() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                    "description": "테스트 펀딩 설명",
                                    "categoryId": 1,
                                    "imageUrl": "http://example.com/image.jpg",
                                    "targetAmount": 0,
                                    "price": 100,
                                    "stock": 10,
                                    "startDate": "2025-10-01 00:00:00",
                                    "endDate": "2025-12-31 23:59:59"
                                }
                                """))
                .andDo(print());

        // then (목표 금액 0원 이하)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("targetAmount: 목표 금액은 0보다 커야 합니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 생성 실패 - 종료일이 시작일 이전")
    void t7() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 펀딩",
                                    "description": "테스트 펀딩 설명",
                                    "categoryId": 1,
                                    "imageUrl": "http://example.com/image.jpg",
                                    "targetAmount": 1000000,
                                    "price": 10000,
                                    "stock": 50,
                                    "startDate": "2025-12-31 23:59:59",
                                    "endDate": "2025-10-01 00:00:00"
                                }
                                """))
                .andDo(print());

        // then (종료일이 시작일 이전)
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("종료일은 시작일 이후여야 합니다."));
    }

    @Test
    @DisplayName("펀딩 조회")
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
                .andExpect(jsonPath("$.data.categoryName").value("테스트 카테고리"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.stock").value(50))
                .andExpect(jsonPath("$.data.targetAmount").value(1_000_000))
                .andExpect(jsonPath("$.data.startDate").exists())
                .andExpect(jsonPath("$.data.endDate").exists());
    }

    @Test
    @DisplayName("GET /api/fundings - 전체 조회")
    void t9() throws Exception {
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
    void t10() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "앨범"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value(containsString("앨범")));
    }

    @Test
    @DisplayName("GET /api/fundings?keyword=포토북 - 포토북 검색")
    void t11() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "포토북"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value(containsString("포토북")));
    }

    @Test
    @DisplayName("GET /api/fundings?statuses=OPEN - 상태 필터")
    void t12() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("statuses", "OPEN"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings?minPrice=30000 - 최소 가격 필터")
    void t13() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("minPrice", "30000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2))); // 35000, 80000원 옵션
    }

    @Test
    @DisplayName("GET /api/fundings?maxPrice=20000 - 최대 가격 필터")
    void t14() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("maxPrice", "20000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2))); // 15000원 옵션
    }

    @Test
    @DisplayName("GET /api/fundings?minPrice=10000&maxPrice=50000 - 가격 범위")
    void t15() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("minPrice", "10000")
                        .param("maxPrice", "50000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3))); // 15000, 35000원 옵션
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=popular - 인기순 정렬")
    void t16() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "popular"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].title").value("신규 앨범 제작")); // 참여자 50명
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=deadline - 마감임박순")
    void t17() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "deadline"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].title").value("한정판 포토북")); // 5일 남음
    }

    @Test
    @DisplayName("GET /api/fundings?sortBy=highAmount - 목표금액 높은순")
    void t18() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "highAmount"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].title").value("프리미엄 굿즈"))
                .andExpect(jsonPath("$.data.content[0].targetAmount").value(2000000));
    }

    @Test
    @DisplayName("GET /api/fundings?page=0&size=2 - 페이징")
    void t19() throws Exception {
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
    void t20() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("statuses", "OPEN")
                        .param("keyword", "앨범")
                        .param("minPrice", "10000")
                        .param("maxPrice", "200000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("신규 앨범 제작"));
    }

    @Test
    @DisplayName("GET /api/fundings?keyword=없는검색어 - 검색 결과 없음")
    void t21() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "존재하지않는검색어"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/fundings - 응답 DTO 필드 검증")
    void t22() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].title").exists())
                .andExpect(jsonPath("$.data.content[0].categoryName").exists())
                .andExpect(jsonPath("$.data.content[0].imageUrl").exists())
                .andExpect(jsonPath("$.data.content[0].authorName").exists())
                .andExpect(jsonPath("$.data.content[0].targetAmount").exists())
                .andExpect(jsonPath("$.data.content[0].currentAmount").exists())
                .andExpect(jsonPath("$.data.content[0].progress").exists())
                .andExpect(jsonPath("$.data.content[0].remainingDays").exists());
    }

    @Test
    @DisplayName("GET /api/fundings - Page 메타데이터 검증")
    void t23() throws Exception {
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
    void t24() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("sortBy", "recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings - sortBy 파라미터 없을 때 (기본값 적용)")
    void t25() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings?statuses=OPEN,CLOSED - 여러 상태 필터")
    void t26() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("statuses", "OPEN,CLOSED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    @DisplayName("GET /api/fundings?page=1 - 2페이지 조회")
    void t27() throws Exception {
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
    void t28() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("minPrice", "100000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/fundings - URL 인코딩된 한글 키워드")
    void t29() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "앨범"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value(containsString("앨범")));
    }

    @Test
    @DisplayName("GET /api/fundings - 진행률 계산 검증")
    void t30() throws Exception {
        mvc.perform(get("/api/fundings")
                        .param("keyword", "앨범"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].progress").isNumber())
                .andExpect(jsonPath("$.data.content[0].progress").value(greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("GET /api/fundings - 남은 일수 계산 검증")
    void t31() throws Exception {
        mvc.perform(get("/api/fundings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].remainingDays").isNumber());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 수정 성공 - 제목/이미지/종료일")
    void t32() throws Exception {
        ResultActions ra = mvc.perform(patch("/api/fundings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "제목 수정",
                                  "description": "설명 수정",
                                  "targetAmount": 1200000,
                                  "endDate": "2030-12-31T12:30:00"
                                }
                                """.formatted(1L)))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("펀딩이 수정되었습니다."))
                .andExpect(jsonPath("$.data.title").value("제목 수정"))
                .andExpect(jsonPath("$.data.targetAmount").value(1200000))
                .andExpect(jsonPath("$.data.endDate").value("2030-12-31 12:30:00"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 수정 실패 - 종료일이 기존 종료일보다 이전")
    void t33() throws Exception {

        ResultActions ra = mvc.perform(patch("/api/fundings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "endDate": "%s" }
                                """.formatted(LocalDateTime.now().minusDays(30).toString())))
                .andDo(print());

        ra.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("종료일은 기존 종료일보다 이후여야 합니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 수정 실패 - 목표 금액이 0 이하")
    void t34() throws Exception {
        ResultActions ra = mvc.perform(patch("/api/fundings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "targetAmount": 0 }
                                """))
                .andDo(print());

        ra.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("목표 금액은 0보다 커야 합니다."));
    }

//    @Test
//    @WithUserDetails("user1@user.com")
//    @DisplayName("펀딩 수정 실패 - 참여자 존재 시 목표 금액 변경 불가")
//    void t35() throws Exception {
//        // 준비: 참여자 존재 상태로 만들기 (도메인 메서드 사용)
//        activeFunding.increaseParticipantCount(1);
//        fundingRepository.saveAndFlush(activeFunding);
//
//        ResultActions ra = mvc.perform(patch("/api/fundings/{id}", activeFunding.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                { "targetAmount": 2000000 }
//                                """))
//                .andDo(print());
//
//        ra.andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.msg").value("참여자가 있으면 목표 금액을 수정할 수 없습니다."));
//    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 수정 실패 - 종료된 펀딩은 종료일 수정 불가")
    void t38() throws Exception {
        Funding funding = fundingRepository.findById(1L).orElseThrow();
        funding.close();
        fundingRepository.save(funding);

        ResultActions ra = mvc.perform(patch("/api/fundings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "endDate": "2030-01-01T00:00:00" }
                                """))
                .andDo(print());

        ra.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("진행 중인 펀딩만 종료일을 수정할 수 있습니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 삭제")
    void t39() throws Exception {
        ResultActions ra = mvc.perform(delete("/api/fundings/4"))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("펀딩이 삭제되었습니다."));
    }
}
