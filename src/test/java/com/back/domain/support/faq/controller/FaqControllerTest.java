package com.back.domain.support.faq.controller;

import com.back.domain.support.faq.entity.Faq;
import com.back.domain.support.faq.entity.FaqCategory;
import com.back.domain.support.faq.repository.FaqRepository;
import com.back.domain.user.repository.UserRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("FAQ 컨트롤러 테스트")
public class FaqControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FaqRepository faqRepository;

    @Test
    @DisplayName("1. FAQ 목록 조회 - 성공")
    void t1() throws Exception {
        // given - FAQ 생성
        Faq faq1 = Faq.builder()
                .question("회원가입은 어떻게 하나요?")
                .answer("회원가입 버튼을 클릭하세요.")
                .category(FaqCategory.ACCOUNT)
                .build();
        faqRepository.save(faq1);

        Faq faq2 = Faq.builder()
                .question("배송은 얼마나 걸리나요?")
                .answer("2-3일 소요됩니다.")
                .category(FaqCategory.DELIVERY)
                .build();
        faqRepository.save(faq2);

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/faqs")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("FAQ 목록 조회 성공"))
                .andExpect(jsonPath("$.data.faqs").isArray())
                .andExpect(jsonPath("$.data.faqs.length()").value(2))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("2. FAQ 목록 조회 - 빈 목록")
    void t2() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/support/faqs")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("FAQ 목록 조회 성공"))
                .andExpect(jsonPath("$.data.faqs").isArray())
                .andExpect(jsonPath("$.data.faqs.length()").value(0));
    }

    @Test
    @DisplayName("3. FAQ 목록 조회 - 카테고리 필터링")
    void t3() throws Exception {
        // given
        Faq faq1 = Faq.builder()
                .question("회원가입 질문")
                .answer("답변")
                .category(FaqCategory.ACCOUNT)
                .build();
        faqRepository.save(faq1);

        Faq faq2 = Faq.builder()
                .question("배송 질문")
                .answer("답변")
                .category(FaqCategory.DELIVERY)
                .build();
        faqRepository.save(faq2);

        // when - ACCOUNT 카테고리만 조회
        ResultActions resultActions = mvc.perform(get("/api/support/faqs")
                        .param("category", "ACCOUNT")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.faqs.length()").value(1))
                .andExpect(jsonPath("$.data.faqs[0].category").value("ACCOUNT"));
    }

    @Test
    @DisplayName("4. FAQ 상세 조회 - 성공")
    void t4() throws Exception {
        // given
        Faq faq = Faq.builder()
                .question("테스트 질문")
                .answer("테스트 답변")
                .category(FaqCategory.PRODUCT)
                .build();
        faqRepository.save(faq);

        Long initialViewCount = faq.getViewCount();

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/faqs/" + faq.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("FAQ 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(faq.getId()))
                .andExpect(jsonPath("$.data.question").value("테스트 질문"))
                .andExpect(jsonPath("$.data.answer").value("테스트 답변"))
                .andExpect(jsonPath("$.data.category").value("PRODUCT"))
                .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));
    }

    @Test
    @DisplayName("5. FAQ 상세 조회 - 실패 (존재하지 않는 FAQ)")
    void t5() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/support/faqs/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 FAQ입니다."));
    }

    // ========================================
    // 관리자 전용 API 테스트
    // ========================================

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("6. FAQ 생성 - 성공 (관리자)")
    void t6() throws Exception {
        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", "새로운 FAQ 질문");
        requestBody.put("answer", "새로운 FAQ 답변");
        requestBody.put("category", "FUNDING");

        // when
        ResultActions resultActions = mvc.perform(post("/api/support/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("FAQ가 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("7. FAQ 생성 - 실패 (미인증)")
    void t7() throws Exception {
        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", "질문");
        requestBody.put("answer", "답변");
        requestBody.put("category", "ACCOUNT");

        // when
        ResultActions resultActions = mvc.perform(post("/api/support/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("8. FAQ 생성 - 실패 (일반 사용자)")
    void t8() throws Exception {
        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", "질문");
        requestBody.put("answer", "답변");
        requestBody.put("category", "ACCOUNT");

        // when
        ResultActions resultActions = mvc.perform(post("/api/support/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("9. FAQ 생성 - 실패 (필수 필드 누락)")
    void t9() throws Exception {
        // given - 질문 누락
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("answer", "답변만 있음");
        requestBody.put("category", "ACCOUNT");

        // when
        ResultActions resultActions = mvc.perform(post("/api/support/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("10. FAQ 수정 - 성공")
    void t10() throws Exception {
        // given
        Faq faq = Faq.builder()
                .question("수정 전 질문")
                .answer("수정 전 답변")
                .category(FaqCategory.ACCOUNT)
                .build();
        faqRepository.save(faq);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", "수정 후 질문");
        requestBody.put("answer", "수정 후 답변");
        requestBody.put("category", "DELIVERY");

        // when
        ResultActions resultActions = mvc.perform(put("/api/support/faqs/" + faq.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("FAQ가 성공적으로 수정되었습니다."));

        // 실제로 수정되었는지 확인
        Faq updatedFaq = faqRepository.findById(faq.getId()).orElseThrow();
        assertThat(updatedFaq.getQuestion()).isEqualTo("수정 후 질문");
        assertThat(updatedFaq.getAnswer()).isEqualTo("수정 후 답변");
        assertThat(updatedFaq.getCategory()).isEqualTo(FaqCategory.DELIVERY);
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("11. FAQ 수정 - 실패 (존재하지 않는 FAQ)")
    void t11() throws Exception {
        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", "수정 질문");
        requestBody.put("answer", "수정 답변");
        requestBody.put("category", "ACCOUNT");

        // when
        ResultActions resultActions = mvc.perform(put("/api/support/faqs/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 FAQ입니다."));
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("12. FAQ 삭제 - 성공")
    void t12() throws Exception {
        // given
        Faq faq = Faq.builder()
                .question("삭제할 FAQ")
                .answer("삭제할 답변")
                .category(FaqCategory.ETC)
                .build();
        faqRepository.save(faq);

        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/faqs/" + faq.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("FAQ가 삭제되었습니다."));

        // 실제로 삭제되었는지 확인
        assertThat(faqRepository.findById(faq.getId())).isEmpty();
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("13. FAQ 삭제 - 실패 (존재하지 않는 FAQ)")
    void t13() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/faqs/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 FAQ입니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("14. FAQ 삭제 - 실패 (일반 사용자)")
    void t14() throws Exception {
        // given
        Faq faq = Faq.builder()
                .question("삭제할 FAQ")
                .answer("답변")
                .category(FaqCategory.ACCOUNT)
                .build();
        faqRepository.save(faq);

        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/faqs/" + faq.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. FAQ 삭제 - 실패 (미인증)")
    void t15() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/faqs/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }
}
