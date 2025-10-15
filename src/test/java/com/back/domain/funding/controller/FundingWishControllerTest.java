package com.back.domain.funding.controller;


import com.back.domain.funding.service.FundingWishService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("펀딩 찜 컨트롤러 테스트")
public class FundingWishControllerTest {

    @Autowired
    private FundingWishService fundingWishService;
    @Autowired
    MockMvc mvc;

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜하기")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("찜 목록에 추가되었습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜하기 - 이미 찜한 상태")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ResultActions ra = mvc.perform(post("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ra.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("이미 찜한 펀딩입니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜해제")
    void t3() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ResultActions ra = mvc.perform(delete("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("찜 목록에서 제거되었습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜해제 - 찜 안된 상태")
    void t4() throws Exception {
        ResultActions ra = mvc.perform(delete("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ra.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("찜하지 않은 펀딩입니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜 여부 확인 - 찜 안한 상태")
    void t5() throws Exception {
        ResultActions ra = mvc.perform(get("/api/fundings/1/wish/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("false"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜 여부 확인 - 찜 한 상태")
    void t6() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());
        ResultActions ra = mvc.perform(get("/api/fundings/1/wish/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("true"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("찜 목록 조회")
    void t7() throws Exception {
        ResultActions resultActions1 = mvc.perform(post("/api/fundings/1/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());
        ResultActions resultActions2 = mvc.perform(post("/api/fundings/2/wish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());
        ResultActions ra = mvc.perform(get("/api/fundings/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }
}

