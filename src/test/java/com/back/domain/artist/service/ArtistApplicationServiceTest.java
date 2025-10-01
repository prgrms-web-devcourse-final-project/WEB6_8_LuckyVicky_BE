package com.back.domain.artist.service;

import com.back.domain.artist.dto.request.ArtistApplicationRequest;
import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.DocumentType;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.S3FileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistApplicationService 단위 테스트")
public class ArtistApplicationServiceTest {

    @Mock
    private ArtistApplicationRepository artistApplicationRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks
    private ArtistApplicationService artistApplicationService;

    private User testUser;
    private ArtistApplicationRequest validRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = User.createLocalUser(
                "artist@test.com",
                "encodedPassword",
                "테스트작가",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트용 Request 생성
        Map<DocumentType, List<S3FileRequest>> documents = new HashMap<>();
        documents.put(DocumentType.BUSINESS_LICENSE, List.of(
                new S3FileRequest("https://s3.../business.pdf", null, "docs/uuid1.pdf", "사업자등록증.pdf")
        ));
        documents.put(DocumentType.TELECOM_CERTIFICATION, List.of(
                new S3FileRequest("https://s3.../telecom.pdf", null, "docs/uuid2.pdf", "통신판매업신고증.pdf")
        ));
        documents.put(DocumentType.PORTFOLIO, List.of(
                new S3FileRequest("https://s3.../portfolio.jpg", null, "docs/uuid3.jpg", "포트폴리오.jpg")
        ));

        validRequest = new ArtistApplicationRequest(
                "홍길동",
                "artist@test.com",
                "010-1234-5678",
                "아티스트홍",
                "123-45-67890",
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "2024-서울강남-00001",
                documents,
                "홍길동아트",
                "인스타그램@artist",
                "도자기, 그림",
                "010-9876-5432",
                "카카오뱅크",
                "1234567890",
                "홍길동"
        );
    }

    @Nested
    @DisplayName("작가 신청서 생성 테스트")
    class CreateApplicationTest {

        @Test
        @DisplayName("정상적인 작가 신청서 생성 성공")
        void createApplication_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(false);

            ArtistApplication savedApplication = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName(validRequest.ownerName())
                    .email(validRequest.email())
                    .phone(validRequest.phone())
                    .artistName(validRequest.artistName())
                    .businessNumber(validRequest.businessNumber())
                    .businessAddress(validRequest.businessAddress())
                    .businessAddressDetail(validRequest.businessAddressDetail())
                    .businessZipCode(validRequest.businessZipCode())
                    .telecomSalesNumber(validRequest.telecomSalesNumber())
                    .build();
            ReflectionTestUtils.setField(savedApplication, "id", 1L);

            given(artistApplicationRepository.save(any(ArtistApplication.class)))
                    .willReturn(savedApplication);

            // when
            Long applicationId = artistApplicationService.createApplication(1L, validRequest);

            // then
            assertThat(applicationId).isEqualTo(1L);
            verify(userRepository).findById(1L);
            verify(artistApplicationRepository).existsByUserIdAndStatus(1L, ApplicationStatus.PENDING);
            verify(artistApplicationRepository).save(any(ArtistApplication.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 신청 실패")
        void createApplication_UserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(999L, validRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("중복 신청으로 실패 (이미 PENDING 상태 신청서 존재)")
        void createApplication_DuplicateApplication() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(1L, validRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 심사 대기 중인 신청서가 있습니다.");
                    });
        }

        @Test
        @DisplayName("필수 서류 누락으로 신청 실패 (사업자등록증 없음)")
        void createApplication_MissingBusinessLicense() {
            // given
            Map<DocumentType, List<S3FileRequest>> incompleteDocuments = new HashMap<>();
            incompleteDocuments.put(DocumentType.TELECOM_CERTIFICATION, List.of(
                    new S3FileRequest("https://s3.../telecom.pdf", null, "docs/uuid2.pdf", "통신판매업신고증.pdf")
            ));

            ArtistApplicationRequest invalidRequest = new ArtistApplicationRequest(
                    "홍길동", "artist@test.com", "010-1234-5678", "아티스트홍",
                    "123-45-67890", "서울시 강남구", "테헤란로 123", "12345",
                    "2024-서울강남-00001", incompleteDocuments,
                    "홍길동아트", "인스타그램@artist", "도자기, 그림", "010-9876-5432",
                    "카카오뱅크", "1234567890", "홍길동"
            );

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(1L, invalidRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).contains("필수 서류가 누락되었습니다");
                    });
        }

        @Test
        @DisplayName("필수 서류 누락으로 신청 실패 (통신판매업신고증 없음)")
        void createApplication_MissingTelecomCertification() {
            // given
            Map<DocumentType, List<S3FileRequest>> incompleteDocuments = new HashMap<>();
            incompleteDocuments.put(DocumentType.BUSINESS_LICENSE, List.of(
                    new S3FileRequest("https://s3.../business.pdf", null, "docs/uuid1.pdf", "사업자등록증.pdf")
            ));

            ArtistApplicationRequest invalidRequest = new ArtistApplicationRequest(
                    "홍길동", "artist@test.com", "010-1234-5678", "아티스트홍",
                    "123-45-67890", "서울시 강남구", "테헤란로 123", "12345",
                    "2024-서울강남-00001", incompleteDocuments,
                    "홍길동아트", "인스타그램@artist", "도자기, 그림", "010-9876-5432",
                    "카카오뱅크", "1234567890", "홍길동"
            );

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(1L, invalidRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).contains("필수 서류가 누락되었습니다");
                    });
        }
    }

    @Nested
    @DisplayName("내 신청서 목록 조회 테스트")
    class GetMyApplicationsTest {

        @Test
        @DisplayName("내 신청서 목록 조회 성공")
        void getMyApplications_Success() {
            // given
            given(userRepository.existsById(1L)).willReturn(true);

            ArtistApplication application1 = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName("홍길동")
                    .email("artist@test.com")
                    .phone("010-1234-5678")
                    .artistName("아티스트홍")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00001")
                    .build();
            ReflectionTestUtils.setField(application1, "id", 1L);

            ArtistApplication application2 = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName("홍길동")
                    .email("artist@test.com")
                    .phone("010-1234-5678")
                    .artistName("아티스트홍2")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00002")
                    .build();
            ReflectionTestUtils.setField(application2, "id", 2L);

            given(artistApplicationRepository.findByUserIdOrderByCreateDateDesc(1L))
                    .willReturn(Arrays.asList(application1, application2));

            // when
            List<ArtistApplicationSimpleResponse> responses =
                    artistApplicationService.getMyApplications(1L);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).artistName()).isEqualTo("아티스트홍");
            assertThat(responses.get(1).artistName()).isEqualTo("아티스트홍2");
            verify(artistApplicationRepository).findByUserIdOrderByCreateDateDesc(1L);
        }

        @Test
        @DisplayName("신청서가 없는 경우 빈 목록 반환")
        void getMyApplications_EmptyList() {
            // given
            given(userRepository.existsById(1L)).willReturn(true);

            // when
            List<ArtistApplicationSimpleResponse> responses =
                    artistApplicationService.getMyApplications(1L);

            // then
            assertThat(responses).isEmpty();
            verify(artistApplicationRepository).findByUserIdOrderByCreateDateDesc(1L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 목록 조회 실패")
        void getMyApplications_UserNotFound() {
            // given
            given(userRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getMyApplications(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });

            verify(userRepository).existsById(999L);
        }
    }

    @Nested
    @DisplayName("신청서 상세 조회 테스트")
    class GetApplicationByIdTest {

        @Test
        @DisplayName("본인 신청서 상세 조회 성공")
        void getApplicationById_Success() {
            // given
            ArtistApplication application = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName("홍길동")
                    .email("artist@test.com")
                    .phone("010-1234-5678")
                    .artistName("아티스트홍")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00001")
                    .build();
            ReflectionTestUtils.setField(application, "id", 1L);

            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when
            ArtistApplicationResponse response =
                    artistApplicationService.getApplicationById(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.artistName()).isEqualTo("아티스트홍");
            assertThat(response.userId()).isEqualTo(1L);
            verify(artistApplicationRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 신청서 조회 실패")
        void getApplicationById_NotFound() {
            // given
            given(artistApplicationRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getApplicationById(1L, 999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("신청서를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("다른 사람의 신청서 조회 실패")
        void getApplicationById_Forbidden() {
            // given
            User otherUser = User.createLocalUser(
                    "other@test.com",
                    "encodedPassword",
                    "다른사용자",
                    "010-9999-9999"
            );
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            ArtistApplication application = ArtistApplication.builder()
                    .user(otherUser) // 다른 사용자의 신청서
                    .ownerName("홍길동")
                    .email("other@test.com")
                    .phone("010-9999-9999")
                    .artistName("다른작가")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00001")
                    .build();
            ReflectionTestUtils.setField(application, "id", 1L);

            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getApplicationById(1L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("403");
                        assertThat(serviceEx.getMsg()).isEqualTo("본인의 신청서만 조회할 수 있습니다.");
                    });
        }
    }

}
