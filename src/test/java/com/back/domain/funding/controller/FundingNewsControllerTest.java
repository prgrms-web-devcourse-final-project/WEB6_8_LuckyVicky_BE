package com.back.domain.funding.controller;

import com.back.domain.funding.repository.FundingNewsRepository;
import com.back.domain.funding.service.FundingNewsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("펀딩 새소식 컨트롤러 테스트")
public class FundingNewsControllerTest {

    @Autowired
    FundingNewsRepository fundingNewsRepository;
    @Autowired
    FundingNewsService fundingNewsService;
    @Autowired
    MockMvc mvc;

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 새소식 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "새소식 입니다.",
                                    "content": "내용입니다.",
                                    "imageUrl": "string"
                                }
                                """))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("새소식이 등록되었습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("펀딩 새소식 생성 - 권한 없음")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "새소식 입니다.",
                                    "content": "내용입니다.",
                                    "imageUrl": "string"
                                }
                                """))
                .andDo(print());

        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("펀딩 새소식 삭제")
    void t3() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/fundings/1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "새소식 입니다.",
                                    "content": "내용입니다.",
                                    "imageUrl": "string"
                                }
                                """))
                .andDo(print());
        ResultActions ra = mvc.perform(delete("/api/fundings/1/news/2"))
                .andDo(print());

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("새소식이 삭제되었습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("펀딩 새소식 삭제 - 권한없음")
    void t4() throws Exception {
        ObjectMapper om = new ObjectMapper();

        String createResponse = mvc.perform(post("/api/fundings/1/news")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("user1@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""

                                {
                              "title": "삭제 권한 테스트",
                              "content": "user1이 작성한 새소식",
                              "imageUrl": "http://example.com/test.png"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = om.readTree(createResponse);
        long newsId = root.get("data").asLong();

        ResultActions deleteAction = mvc.perform(delete("/api/fundings/{fundingId}/news/{newsId}", 1L, newsId))
                .andDo(print());

        deleteAction.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }
}


