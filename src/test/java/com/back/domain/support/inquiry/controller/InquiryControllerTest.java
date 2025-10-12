package com.back.domain.support.inquiry.controller;

import com.back.domain.support.inquiry.entity.Inquiry;
import com.back.domain.support.inquiry.entity.InquiryCategory;
import com.back.domain.support.inquiry.repository.InquiryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("문의 컨트롤러 테스트")
public class InquiryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Test
    @DisplayName("1. 공개 문의 목록 조회 - 성공 (비로그인)")
    void t1() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry1 = Inquiry.builder()
                .user(user)
                .title("배송 문의")
                .content("배송이 언제 되나요?")
                .category(InquiryCategory.DELIVERY)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry1);

        Inquiry inquiry2 = Inquiry.builder()
                .user(user)
                .title("비밀 문의")
                .content("비밀 내용")
                .category(InquiryCategory.PAYMENT)
                .isSecret(true)
                .build();
        inquiryRepository.save(inquiry2);

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/public")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then - 공개 문의만 조회됨
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.inquiries").isArray())
                .andExpect(jsonPath("$.data.inquiries.length()").value(1))
                .andExpect(jsonPath("$.data.inquiries[0].title").value("배송 문의"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("2. 문의 목록 조회 - 성공 (로그인)")
    void t2() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry1 = Inquiry.builder()
                .user(user)
                .title("내 공개 문의")
                .content("공개 내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry1);

        Inquiry inquiry2 = Inquiry.builder()
                .user(user)
                .title("내 비밀 문의")
                .content("비밀 내용")
                .category(InquiryCategory.PAYMENT)
                .isSecret(true)
                .build();
        inquiryRepository.save(inquiry2);

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then - 공개 + 내 비밀문의 모두 조회됨
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.inquiries.length()").value(2));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("3. 내 문의만 조회 - 성공")
    void t3() throws Exception {
        // given
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();
        User user2 = userRepository.findByEmail("user2@user.com").orElseThrow();

        Inquiry myInquiry = Inquiry.builder()
                .user(user1)
                .title("내 문의")
                .content("내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(myInquiry);

        Inquiry otherInquiry = Inquiry.builder()
                .user(user2)
                .title("다른 사람 문의")
                .content("내용")
                .category(InquiryCategory.DELIVERY)
                .isSecret(false)
                .build();
        inquiryRepository.save(otherInquiry);

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/my")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then - 내 문의만 조회됨
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiries.length()").value(1))
                .andExpect(jsonPath("$.data.inquiries[0].title").value("내 문의"));
    }

    @Test
    @DisplayName("4. 문의 상세 조회 - 성공 (공개 문의)")
    void t4() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title("배송 문의")
                .content("배송 언제 되나요?")
                .category(InquiryCategory.DELIVERY)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry);

        Long initialViewCount = inquiry.getViewCount();

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/" + inquiry.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(inquiry.getId()))
                .andExpect(jsonPath("$.data.title").value("배송 문의"))
                .andExpect(jsonPath("$.data.category").value("DELIVERY"))
                .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));
    }

    @Test
    @DisplayName("5. 문의 상세 조회 - 실패 (존재하지 않는 문의)")
    void t5() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("6. 문의 작성 - 성공")
    void t6() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/inquiries")
                        .param("category", "PRODUCT")
                        .param("title", "상품 문의")
                        .param("content", "재고 있나요?")
                        .param("isSecret", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("문의가 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("7. 문의 작성 - 실패 (미인증)")
    void t7() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/inquiries")
                        .param("category", "PRODUCT")
                        .param("title", "문의")
                        .param("content", "내용")
                        .param("isSecret", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("8. 문의 수정 - 성공")
    void t8() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title("수정 전 제목")
                .content("수정 전 내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry);

        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/inquiries/" + inquiry.getId())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("category", "DELIVERY")
                        .param("title", "수정 후 제목")
                        .param("content", "수정 후 내용")
                        .param("isSecret", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("문의가 성공적으로 수정되었습니다."));

        Inquiry updated = inquiryRepository.findById(inquiry.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("수정 후 제목");
        assertThat(updated.getCategory()).isEqualTo(InquiryCategory.DELIVERY);
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("9. 문의 삭제 - 성공")
    void t9() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title("삭제할 문의")
                .content("내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry);

        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/inquiries/" + inquiry.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("문의가 삭제되었습니다."));

        assertThat(inquiryRepository.findById(inquiry.getId())).isEmpty();
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("10. 전체 문의 목록 조회 - 성공 (관리자)")
    void t10() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry1 = Inquiry.builder()
                .user(user)
                .title("공개 문의")
                .content("내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry1);

        Inquiry inquiry2 = Inquiry.builder()
                .user(user)
                .title("비밀 문의")
                .content("비밀 내용")
                .category(InquiryCategory.PAYMENT)
                .isSecret(true)
                .build();
        inquiryRepository.save(inquiry2);

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/admin/all")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then - 비밀문의 포함 모두 조회됨
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.inquiries.length()").value(2));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("11. 전체 문의 목록 조회 - 실패 (일반 사용자)")
    void t11() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/admin/all")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("12. 댓글 작성 - 성공")
    void t12() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title("문의")
                .content("내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry);

        // when
        ResultActions resultActions = mvc.perform(post("/api/support/inquiries/" + inquiry.getId() + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "추가 질문입니다."
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("댓글이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("13. 댓글 작성 - 실패 (미인증)")
    void t13() throws Exception {
        // given
        User user = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title("문의")
                .content("내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry);

        // when
        ResultActions resultActions = mvc.perform(post("/api/support/inquiries/" + inquiry.getId() + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "댓글 내용"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("14. 비밀문의 상세 조회 - 실패 (권한 없음)")
    void t14() throws Exception {
        // given
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry secretInquiry = Inquiry.builder()
                .user(user1)
                .title("user1의 비밀 문의")
                .content("비밀 내용")
                .category(InquiryCategory.PAYMENT)
                .isSecret(true)
                .build();
        inquiryRepository.save(secretInquiry);

        // when - user2가 user1의 비밀문의 조회 시도
        ResultActions resultActions = mvc.perform(get("/api/support/inquiries/" + secretInquiry.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("15. 다른 사용자 문의 수정 - 실패 (권한 없음)")
    void t15() throws Exception {
        // given
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();

        Inquiry inquiry = Inquiry.builder()
                .user(user1)
                .title("user1의 문의")
                .content("내용")
                .category(InquiryCategory.PRODUCT)
                .isSecret(false)
                .build();
        inquiryRepository.save(inquiry);

        // when - user2가 user1의 문의 수정 시도
        ResultActions resultActions = mvc.perform(multipart("/api/support/inquiries/" + inquiry.getId())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("category", "DELIVERY")
                        .param("title", "수정 시도")
                        .param("content", "수정 내용")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));
    }
}