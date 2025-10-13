package com.back.domain.funding.controller;

import com.back.domain.funding.repository.FundingCommunityRepository;
import com.back.domain.funding.service.FundingCommunityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("펀딩 커뮤니티 컨트롤러 테스트")
public class FundingCommunityControllerTest {

    @Autowired
    FundingCommunityRepository fundingCommunityRepository;
    @Autowired
    FundingCommunityService fundingCommunityService;
    @Autowired
    MockMvc mvc;

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 커뮤니티 글 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "내용입니다."
                                }
                                """))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("커뮤니티 글이 등록되었습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("펀딩 커뮤니티 글 생성 - 일반 유저")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "내용입니다."
                                }
                                """))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("커뮤니티 글이 등록되었습니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 커뮤니티 글 삭제")
    void t3() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "내용입니다."
                                }
                                """))
                .andDo(print());
        ResultActions ra = mvc.perform(delete("/api/fundings/1/communities/3"))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("커뮤니티 글이 삭제되었습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("펀딩 커뮤니티 글 삭제 - 권한없음")
    void t4() throws Exception {
        ObjectMapper om = new ObjectMapper();

        String createResponse = mvc.perform(post("/api/fundings/1/communities")
                        .with(SecurityMockMvcRequestPostProcessors.user("user1@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""

                                {
                              "content": "user1이 작성한 글"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = om.readTree(createResponse);
        long newsId = root.get("data").asLong();

        ResultActions deleteAction = mvc.perform(delete("/api/fundings/{fundingId}/communities/{newsId}", 1L, newsId))
                .andDo(print());

        deleteAction.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("펀딩 커뮤니티 글 삭제 - 괸리자")
    void t5() throws Exception {
        ObjectMapper om = new ObjectMapper();

        String createResponse = mvc.perform(post("/api/fundings/1/communities")
                        .with(SecurityMockMvcRequestPostProcessors.user("user1@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""

                                {
                              "content": "user1이 작성한 글"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = om.readTree(createResponse);
        long newsId = root.get("data").asLong();

        ResultActions deleteAction = mvc.perform(delete("/api/fundings/{fundingId}/communities/{newsId}", 1L, newsId))
                .andDo(print());

        deleteAction.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("커뮤니티 글이 삭제되었습니다."));
    }
}
