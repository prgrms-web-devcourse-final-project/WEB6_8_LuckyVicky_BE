package com.back.domain.user.controller;


import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.user.service.UserService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("사용자 컨트롤러 테스트")
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("1. 내 프로필 조회 - 성공")
    void t1() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("프로필 조회 성공"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.role").exists())
                .andExpect(jsonPath("$.data.grade").exists());
    }

    @Test
    @DisplayName("2. 내 프로필 조회 - 실패 (미인증)")
    void t2() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("3. 내 정보 수정 - 성공 (비밀번호 제외)")
    void t3() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileImageUrl": "https://example.com/profile.jpg",
                                  "name": "수정된닉네임",
                                  "phone": "010-1234-5678",
                                  "address": "서울시 강남구",
                                  "detailAddress": "테헤란로 123",
                                  "zipCode": "12345"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("사용자 정보 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("수정된닉네임"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.data.detailAddress").value("테헤란로 123"))
                .andExpect(jsonPath("$.data.zipCode").value("12345"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("4. 내 정보 수정 - 성공 (비밀번호 포함)")
    void t4() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileImageUrl": "https://example.com/profile.jpg",
                                  "name": "수정된닉네임2",
                                  "phone": "010-9876-5432",
                                  "address": "서울시 서초구",
                                  "detailAddress": "강남대로 456",
                                  "zipCode": "54321",
                                  "password": "NewPass123!@",
                                  "passwordConfirm": "NewPass123!@"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("사용자 정보 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("수정된닉네임2"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("5. 내 정보 수정 - 실패 (비밀번호 불일치)")
    void t5() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정된닉네임",
                                  "phone": "010-1234-5678",
                                  "password": "NewPass123!@",
                                  "passwordConfirm": "DifferentPass123!@"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("비밀번호와 비밀번호 확인이 일치하지 않습니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("6. 내 정보 수정 - 실패 (닉네임 중복)")
    void t6() throws Exception {
        // given - user2의 닉네임을 사용 시도
        User user2 = userRepository.findByEmail("user2@user.com").orElseThrow();

        // when
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "phone": "010-1234-5678"
                                }
                                """.formatted(user2.getName())))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("7. 내 정보 수정 - 실패 (닉네임 유효성 검증)")
    void t7() throws Exception {
        // when - 닉네임이 너무 짧음
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "a",
                                  "phone": "010-1234-5678"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("8. 내 정보 수정 - 실패 (전화번호 형식 오류)")
    void t8() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "올바른닉네임",
                                  "phone": "01012345678"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("phone: 전화번호는 010-1234-5678 형식이어야 합니다."));
    }

    @Test
    @DisplayName("9. 내 정보 수정 - 실패 (미인증)")
    void t9() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정된닉네임",
                                  "phone": "010-1234-5678"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("10. 회원 탈퇴 - 성공")
    void t10() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴 성공"));
    }

    @Test
    @DisplayName("11. 회원 탈퇴 - 실패 (미인증)")
    void t11() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("12. 특정 사용자 공개 프로필 조회 - 성공")
    void t12() throws Exception {
        // given
        User user2 = userRepository.findByEmail("user2@user.com").orElseThrow();

        // when
        ResultActions resultActions = mvc.perform(get("/api/users/" + user2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공개 프로필 조회 성공"))
                .andExpect(jsonPath("$.data.userId").value(user2.getId()))
                .andExpect(jsonPath("$.data.name").exists())
                // 공개 프로필이므로 민감 정보는 없어야 함
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.phone").doesNotExist())
                .andExpect(jsonPath("$.data.address").doesNotExist());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("13. 특정 사용자 공개 프로필 조회 - 실패 (존재하지 않는 사용자)")
    void t13() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/users/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("14. 내 정보 수정 - 성공 (일부 필드만 수정)")
    void t14() throws Exception {
        // when - 닉네임만 수정
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "닉네임만수정"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.name").value("닉네임만수정"));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("15. 내 정보 수정 - 실패 (비밀번호 형식 오류)")
    void t15() throws Exception {
        // when - 비밀번호가 너무 짧거나 특수문자 없음
        ResultActions resultActions = mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "올바른닉네임",
                                  "password": "short",
                                  "passwordConfirm": "short"
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").exists());
    }
}