package com.back.domain.artist.controller;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistProfileRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("작가 컨트롤러 테스트")
class ArtistControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtistApplicationRepository artistApplicationRepository;

    @Autowired
    private ArtistProfileRepository artistProfileRepository;

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("1. 작가 신청 - 성공")
    void t1() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(post("/api/artist/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerName": "홍길동",
                                  "email": "artist@example.com",
                                  "phone": "010-1234-5678",
                                  "artistName": "작가홍길동",
                                  "businessNumber": "123-45-67890",
                                  "businessName": "홍길동 작가실",
                                  "businessAddress": "서울시 강남구 테헤란로",
                                  "businessAddressDetail": "123번지 2층",
                                  "businessZipCode": "12345",
                                  "telecomSalesNumber": "2023-서울강남-0001",
                                  "snsAccount": "@artist_hong",
                                  "mainProducts": "회화, 조각",
                                  "managerPhone": "010-9876-5432",
                                  "bankName": "신한은행",
                                  "bankAccount": "110-123-456789",
                                  "accountName": "홍길동",
                                  "documents": {
                                    "BUSINESS_LICENSE": [
                                      {
                                        "fileKey": "documents/business_license.pdf",
                                        "fileName": "사업자등록증.pdf",
                                        "fileUrl": "https://example.com/business_license.pdf"
                                      }
                                    ],
                                    "TELECOM_CERTIFICATION": [
                                      {
                                        "fileKey": "documents/telecom_cert.pdf",
                                        "fileName": "통신판매업신고증.pdf",
                                        "fileUrl": "https://example.com/telecom_cert.pdf"
                                      }
                                    ]
                                  }
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("작가 신청 완료"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("2. 작가 신청 - 실패 (미인증)")
    void t2() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(post("/api/artist/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerName": "홍길동",
                                  "email": "artist@example.com",
                                  "phone": "010-1234-5678",
                                  "artistName": "작가홍길동",
                                  "businessNumber": "123-45-67890",
                                  "businessAddress": "서울시 강남구",
                                  "businessAddressDetail": "123번지",
                                  "businessZipCode": "12345",
                                  "telecomSalesNumber": "2023-서울강남-0001",
                                  "documents": {}
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("3. 작가 신청 - 실패 (필수 서류 누락)")
    void t3() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(post("/api/artist/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerName": "홍길동",
                                  "email": "artist@example.com",
                                  "phone": "010-1234-5678",
                                  "artistName": "작가홍길동",
                                  "businessNumber": "123-45-67890",
                                  "businessAddress": "서울시 강남구",
                                  "businessAddressDetail": "123번지",
                                  "businessZipCode": "12345",
                                  "telecomSalesNumber": "2023-서울강남-0001",
                                  "documents": {
                                    "BUSINESS_LICENSE": [
                                      {
                                        "fileKey": "documents/business_license.pdf",
                                        "fileName": "사업자등록증.pdf",
                                        "fileUrl": "https://example.com/business_license.pdf"
                                      }
                                    ]
                                  }
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("필수 서류가 누락되었습니다.통신판매업신고증"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("4. 작가 신청 - 실패 (필수 필드 누락)")
    void t4() throws Exception {
        // when - ownerName 누락
        ResultActions resultActions = mvc.perform(post("/api/artist/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "artist@example.com",
                                  "phone": "010-1234-5678",
                                  "artistName": "작가홍길동",
                                  "businessNumber": "123-45-67890",
                                  "businessAddress": "서울시 강남구",
                                  "businessAddressDetail": "123번지",
                                  "businessZipCode": "12345",
                                  "telecomSalesNumber": "2023-서울강남-0001",
                                  "documents": {}
                                }
                                """))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("5. 내 작가 신청 목록 조회 - 성공")
    void t5() throws Exception {
        // given - 신청서 생성
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        ArtistApplication application = ArtistApplication.builder()
                .user(user)
                .ownerName("홍길동")
                .email("artist@example.com")
                .phone("010-1234-5678")
                .artistName("작가홍길동")
                .businessNumber("123-45-67890")
                .businessName("홍길동 작가실")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("123번지")
                .businessZipCode("12345")
                .telecomSalesNumber("2023-서울강남-0001")
                .bankName("신한은행")
                .bankAccount("110-123-456789")
                .accountName("홍길동")
                .build();
        artistApplicationRepository.save(application);

        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/application/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("내 작가 신청 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(application.getId()))
                .andExpect(jsonPath("$.data[0].artistName").value("작가홍길동"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("6. 내 작가 신청 목록 조회 - 실패 (미인증)")
    void t6() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/application/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("7. 작가 신청 상세 조회 - 성공")
    void t7() throws Exception {
        // given
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        ArtistApplication application = ArtistApplication.builder()
                .user(user)
                .ownerName("홍길동")
                .email("artist@example.com")
                .phone("010-1234-5678")
                .artistName("작가홍길동")
                .businessNumber("123-45-67890")
                .businessName("홍길동 작가실")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("123번지")
                .businessZipCode("12345")
                .telecomSalesNumber("2023-서울강남-0001")
                .snsAccount("@artist_hong")
                .mainProducts("회화, 조각")
                .bankName("신한은행")
                .bankAccount("110-123-456789")
                .accountName("홍길동")
                .build();
        artistApplicationRepository.save(application);

        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/application/" + application.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("작가 신청 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(application.getId()))
                .andExpect(jsonPath("$.data.ownerName").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("artist@example.com"))
                .andExpect(jsonPath("$.data.artistName").value("작가홍길동"))
                .andExpect(jsonPath("$.data.businessNumber").value("123-45-67890"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("8. 작가 신청 상세 조회 - 실패 (다른 사용자의 신청서)")
    void t8() throws Exception {
        // given - user1의 신청서 생성
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();
        ArtistApplication application = ArtistApplication.builder()
                .user(user1)
                .ownerName("김철수")
                .email("kim@example.com")
                .phone("010-9999-8888")
                .artistName("작가김철수")
                .businessNumber("999-88-77777")
                .businessAddress("서울시 종로구")
                .businessAddressDetail("456번지")
                .businessZipCode("54321")
                .telecomSalesNumber("2023-서울종로-0002")
                .bankName("국민은행")
                .bankAccount("220-987-654321")
                .accountName("김철수")
                .build();
        artistApplicationRepository.save(application);

        // when - user2가 user1의 신청서 조회 시도
        ResultActions resultActions = mvc.perform(get("/api/artist/application/" + application.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("본인의 신청서만 조회할 수 있습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("9. 작가 신청 상세 조회 - 실패 (존재하지 않는 신청서)")
    void t9() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/application/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("신청서를 찾을 수 없습니다."));
    }



    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("10. 작가 신청 취소 - 실패 (승인된 신청서)")
    void t11() throws Exception {
        // given - 승인된 신청서
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        ArtistApplication application = ArtistApplication.builder()
                .user(user)
                .ownerName("홍길동")
                .email("artist@example.com")
                .phone("010-1234-5678")
                .artistName("작가홍길동")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("123번지")
                .businessZipCode("12345")
                .telecomSalesNumber("2023-서울강남-0001")
                .bankName("신한은행")
                .bankAccount("110-123-456789")
                .accountName("홍길동")
                .build();
        artistApplicationRepository.save(application);

        // 승인 처리
        application.approve(1L, "관리자");
        artistApplicationRepository.save(application);

        // when
        ResultActions resultActions = mvc.perform(delete("/api/artist/application/" + application.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("심사 대기 중인 신청서만 취소할 수 있습니다. 현재 상태: APPROVED"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("11. 작가 신청 취소 - 실패 (다른 사용자의 신청서)")
    void t12() throws Exception {
        // given - user1의 신청서
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();
        ArtistApplication application = ArtistApplication.builder()
                .user(user1)
                .ownerName("김철수")
                .email("kim@example.com")
                .phone("010-9999-8888")
                .artistName("작가김철수")
                .businessNumber("999-88-77777")
                .businessAddress("서울시 종로구")
                .businessAddressDetail("456번지")
                .businessZipCode("54321")
                .telecomSalesNumber("2023-서울종로-0002")
                .bankName("국민은행")
                .bankAccount("220-987-654321")
                .accountName("김철수")
                .build();
        artistApplicationRepository.save(application);

        // when - user2가 user1의 신청서 취소 시도
        ResultActions resultActions = mvc.perform(delete("/api/artist/application/" + application.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("본인의 신청서만 접근할 수 있습니다."));
    }

    @Test
    @DisplayName("12. 작가 신청 취소 - 실패 (미인증)")
    void t13() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/artist/application/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("13. 작가 신청 취소 - 실패 (존재하지 않는 신청서)")
    void t14() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/artist/application/99999/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("신청서를 찾을 수 없습니다."));
    }

    @Test
    @WithUserDetails("artist1@artist.com")
    @DisplayName("14. 작가 사업자 정보 조회 - 성공")
    void t15() throws Exception {
        // given
        User user = userRepository.findByEmail("artist1@artist.com").orElseThrow();
        ArtistApplication application = ArtistApplication.builder()
                .user(user)
                .artistName("홍길동작가")
                .businessName("홍길동 작가실")
                .businessNumber("123-45-67890")
                .ownerName("홍길동")
                .phone("010-1234-5678")
                .managerPhone("010-1234-5678")
                .email("artist@example.com")
                .businessAddress("서울시 강남구 테헤란로 123")
                .businessAddressDetail("2층")
                .telecomSalesNumber("2023-서울강남-0001")
                .build();
        artistApplicationRepository.save(application);

        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/business-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("사업자 정보 조회 성공"))
                .andExpect(jsonPath("$.data.businessName").value("홍길동 작가실"))
                .andExpect(jsonPath("$.data.businessNumber").value("123-45-67890"))
                .andExpect(jsonPath("$.data.ownerName").value("홍길동"))
                .andExpect(jsonPath("$.data.asManager").value("홍길동 작가실/010-1234-5678"))
                .andExpect(jsonPath("$.data.email").value("artist@example.com"))
                .andExpect(jsonPath("$.data.businessAddress").value("서울시 강남구 테헤란로 123 2층"))
                .andExpect(jsonPath("$.data.telecomSalesNumber").value("2023-서울강남-0001"));
    }

    @Test
    @DisplayName("15. 전체 작가 목록 조회 - 성공")
    void t16() throws Exception {
        // given - 작가 프로필 생성 (ArtistApplication 필수)
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();
        User artist1 = userRepository.findByEmail("artist1@artist.com").orElseThrow();

        // ArtistApplication 먼저 생성
        ArtistApplication application1 = ArtistApplication.builder()
                .user(user1)
                .artistName("유저1작가")
                .businessName("유저1 작가실")
                .businessNumber("111-11-11111")
                .ownerName("유저1")
                .phone("010-1234-5678")
                .managerPhone("010-1234-5678")
                .email("user1@user.com")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("1층")
                .businessZipCode("12345")
                .telecomSalesNumber("2023-서울강남-0001")
                .build();
        artistApplicationRepository.save(application1);

        ArtistApplication application2 = ArtistApplication.builder()
                .user(artist1)
                .artistName("작가1")
                .businessName("작가1 작가실")
                .businessNumber("222-22-22222")
                .ownerName("작가1")
                .phone("010-2111-1111")
                .managerPhone("010-2111-1111")
                .email("artist1@artist.com")
                .businessAddress("서울시 종로구")
                .businessAddressDetail("2층")
                .businessZipCode("54321")
                .telecomSalesNumber("2023-서울종로-0002")
                .build();
        artistApplicationRepository.save(application2);

        // ArtistProfile 생성
        ArtistProfile profile1 = ArtistProfile.builder()
                .user(user1)
                .artistApplication(application1)  // ✅ 필수
                .artistName("유저1작가")
                .mainProducts("도자기")
                .build();
        artistProfileRepository.save(profile1);

        ArtistProfile profile2 = ArtistProfile.builder()
                .user(artist1)
                .artistApplication(application2)  // ✅ 필수
                .artistName("작가1")
                .mainProducts("회화")
                .build();
        artistProfileRepository.save(profile2);

        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("작가 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].artistId").exists())
                .andExpect(jsonPath("$.data[0].artistName").exists());
    }

    @Test
    @DisplayName("16. 전체 작가 목록 조회 - 빈 목록")
    void t17() throws Exception {
        // when - ArtistProfile이 없는 상태
        ResultActions resultActions = mvc.perform(get("/api/artist/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("작가 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("17. 작가 공개 프로필 조회 - 성공")
    void t18() throws Exception {
        // given
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();

        // ArtistApplication 먼저 생성
        ArtistApplication application = ArtistApplication.builder()
                .user(user1)
                .artistName("유저1작가")
                .businessName("유저1 작가실")
                .businessNumber("111-11-11111")
                .ownerName("유저1")
                .phone("010-1234-5678")
                .managerPhone("010-1234-5678")
                .email("user1@user.com")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("1층")
                .businessZipCode("12345")
                .telecomSalesNumber("2023-서울강남-0001")
                .build();
        artistApplicationRepository.save(application);

        // ArtistProfile 생성
        ArtistProfile profile = ArtistProfile.builder()
                .user(user1)
                .artistApplication(application)  // ✅ 필수
                .artistName("유저1작가")
                .description("전통 도자기를 현대적으로 재해석하는 작가입니다.")
                .mainProducts("도자기, 머그컵")
                .snsAccount("@artist_user1")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        artistProfileRepository.save(profile);

        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/profile/" + profile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("작가 프로필 조회 성공"))
                .andExpect(jsonPath("$.data.artistId").value(profile.getId()))
                .andExpect(jsonPath("$.data.artistName").value("유저1작가"))
                .andExpect(jsonPath("$.data.description").value("전통 도자기를 현대적으로 재해석하는 작가입니다."))
                .andExpect(jsonPath("$.data.mainProducts").value("도자기, 머그컵"))
                .andExpect(jsonPath("$.data.snsAccount").value("@artist_user1"))
                .andExpect(jsonPath("$.data.followerCount").value(0))
                .andExpect(jsonPath("$.data.totalSales").value(0))
                .andExpect(jsonPath("$.data.productCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("18. 작가 공개 프로필 조회 - 실패 (존재하지 않는 작가)")
    void t19() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/profile/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 작가입니다."));
    }

    @Test
    @DisplayName("19. 작가 상품 목록 조회 - 성공 (상품 없음)")
    void t20() throws Exception {
        // given
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();

        // ArtistApplication 먼저 생성
        ArtistApplication application = ArtistApplication.builder()
                .user(user1)
                .artistName("유저1작가")
                .businessName("유저1 작가실")
                .businessNumber("111-11-11111")
                .ownerName("유저1")
                .phone("010-1234-5678")
                .managerPhone("010-1234-5678")
                .email("user1@user.com")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("1층")
                .businessZipCode("12345")
                .telecomSalesNumber("2023-서울강남-0001")
                .build();
        artistApplicationRepository.save(application);

        // ArtistProfile 생성
        ArtistProfile profile = ArtistProfile.builder()
                .user(user1)
                .artistApplication(application)  // ✅ 필수
                .artistName("유저1작가")
                .build();
        artistProfileRepository.save(profile);

        // when - 상품이 없는 상태에서 조회
        ResultActions resultActions = mvc.perform(get("/api/artist/profile/" + user1.getId() + "/products")
                        .param("page", "1")
                        .param("size", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("작가 상품 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("20. 작가 상품 목록 조회 - 실패 (존재하지 않는 작가)")
    void t21() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/artist/profile/99999/products")
                        .param("page", "1")
                        .param("size", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 작가입니다."));
    }

}