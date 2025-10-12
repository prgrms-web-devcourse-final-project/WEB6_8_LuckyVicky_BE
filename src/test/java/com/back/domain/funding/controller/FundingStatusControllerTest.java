package com.back.domain.funding.controller;

import com.back.domain.funding.service.FundingStatusService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("펀딩 상태 관리 컨트롤러 테스트")
public class FundingStatusControllerTest {

    @Autowired
    private FundingStatusService fundingStatusService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("펀딩 종료 - 성공")
    @WithUserDetails("user1@user.com")
    void t1() throws Exception {

        ResultActions resultActions = mvc.perform(put("/api/fundings/1/close")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("펀딩이 종료되었습니다."));
    }

    @Test
    @DisplayName("펀딩 종료 - 실패 - 권한 없음")
    @WithUserDetails("user2@user.com")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/1/close")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("펀딩 종료 - 실패 - 이미 종료된 펀딩")
    @WithUserDetails("user1@user.com")
    void t3() throws Exception {
        Long fundingId = 1L;
        fundingStatusService.closeFunding(fundingId, "user1@user.com");

        ResultActions resultActions = mvc.perform(put("/api/fundings/1/close")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("진행 중인 펀딩만 종료할 수 있습니다."));
    }

    @Test
    @DisplayName("펀딩 취소 - 성공")
    @WithUserDetails("user1@user.com")
    void t4() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/1/cancel")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("펀딩이 취소되었습니다."));
    }

    @Test
    @DisplayName("펀딩 취소 - 실패 - 권한 없음")
    @WithUserDetails("user2@user.com")
    void t5() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/1/cancel")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("펀딩 취소 - 실패 - 이미 종료된 펀딩")
    @WithUserDetails("user1@user.com")
    void t6() throws Exception {
        Long fundingId = 1L;
        fundingStatusService.closeFunding(fundingId, "user1@user.com");

        ResultActions resultActions = mvc.perform(put("/api/fundings/1/cancel")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("이미 완료된 펀딩은 취소할 수 없습니다."));
    }

    @Test
    @DisplayName("펀딩 승인 - 성공")
    @WithUserDetails("admin1@admin.com")
    void t7() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/4/approve")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("펀딩이 승인되었습니다."));
    }

    @Test
    @DisplayName("펀딩 승인 - 실패 - 권한 없음")
    @WithUserDetails("user1@user.com")
    void t8() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/4/approve")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(jsonPath("$.resultCode").value("500"));
    }

    @Test
    @DisplayName("펀딩 거절 - 성공")
    @WithUserDetails("admin1@admin.com")
    void t9() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/4/reject")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("펀딩이 거절되었습니다."));
    }

    @Test
    @DisplayName("펀딩 거절 - 실패 - 권한 없음")
    @WithUserDetails("user1@user.com")
    void t10() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/4/reject")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(jsonPath("$.resultCode").value("500"));

    }

    @Test
    @DisplayName("승인된 펀딩 자동 오픈 - 성공")
    @WithUserDetails("admin1@admin.com")
    void t11() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/open/approved")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("승인된 펀딩 자동 오픈 - 실패 - 권한 없음")
    @WithUserDetails("user1@user.com")
    void t12() throws Exception {
        ResultActions resultActions = mvc.perform(put("/api/fundings/open/approved")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions.andExpect(jsonPath("$.resultCode").value("500"));
    }
}