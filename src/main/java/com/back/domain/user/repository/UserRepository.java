package com.back.domain.user.repository;

import com.back.domain.user.entity.Provider;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // 로그인용
    Optional<User> findByEmail(String email);

    // 중복 체크용 (회원가입시)
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByPhone(String phone);


    // 중복 체크용 (회원정보 수정시 - 본인 제외)
    /* 필요시 주석 해제
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    */

    // OAuth 로그인 시 기존 사용자 확인용
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    // 동일 이메일의 다른 Provider 계정 확인용
    Optional<User> findByEmailAndProvider(String email, Provider provider);

    // 이메일과 Provider로 사용자 찾기
    boolean existsByEmailAndProvider(String email, Provider provider);

    /**
     * 관리자 계정 조회 (알림 발송용)
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = com.back.domain.user.entity.Role.ADMIN OR u.role = com.back.domain.user.entity.Role.ROOT")
    java.util.List<User> findAllAdmins();

    /**
     * 관리자 대시보드 - 일별 사용자 증가 트렌드 조회
     */
    @org.springframework.data.jpa.repository.Query("SELECT new com.back.domain.dashboard.admin.dto.DailyUserGrowthDto(" +
            "CAST(u.createDate AS LocalDate), " +
            "COUNT(u.id)) " +
            "FROM User u " +
            "WHERE u.createDate >= :startDate " +
            "AND u.createDate < :endDate " +
            "GROUP BY CAST(u.createDate AS LocalDate) " +
            "ORDER BY CAST(u.createDate AS LocalDate) ASC")
    java.util.List<com.back.domain.dashboard.admin.dto.DailyUserGrowthDto> findDailyUserGrowth(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate
    );

    /**
     * 관리자 대시보드 - 일별 작가 증가 트렌드 조회
     */
    @org.springframework.data.jpa.repository.Query("SELECT new com.back.domain.dashboard.admin.dto.DailyUserGrowthDto(" +
            "CAST(u.createDate AS LocalDate), " +
            "COUNT(u.id)) " +
            "FROM User u " +
            "WHERE u.createDate >= :startDate " +
            "AND u.createDate < :endDate " +
            "AND u.role = com.back.domain.user.entity.Role.ARTIST " +
            "AND u.isArtistVerified = true " +
            "GROUP BY CAST(u.createDate AS LocalDate) " +
            "ORDER BY CAST(u.createDate AS LocalDate) ASC")
    java.util.List<com.back.domain.dashboard.admin.dto.DailyUserGrowthDto> findDailyArtistGrowth(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate
    );

    /**
     * 관리자 대시보드 - 전체 작가 수 조회 (최적화)
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE u.role = com.back.domain.user.entity.Role.ARTIST AND u.isArtistVerified = true")
    long countArtists();
}
