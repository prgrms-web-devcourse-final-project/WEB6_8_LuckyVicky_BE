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
}
