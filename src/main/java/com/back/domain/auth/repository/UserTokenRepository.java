package com.back.domain.auth.repository;

import com.back.domain.auth.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    // 활성화된 RefreshToken으로 토큰 조회 (토큰 갱신용)
    Optional<UserToken> findByRefreshTokenAndIsActiveTrue(String refreshToken);

    // RefreshToken으로 토큰 조회 (로그아웃용)
    Optional<UserToken> findByRefreshToken(String refreshToken);

    // 토큰 완전 삭제 (전체 로그아웃용)
    @Modifying
    @Transactional
    @Query("DELETE FROM UserToken ut WHERE ut.user.id = :userId")
    void deleteAllRefreshTokenByUserId(@Param("userId") Long userId);

    /**
     * 토큰 비활성화 메서드 -  주석 처리된 부분은 필요에 따라 활성화하여 사용
     */
//    // RefreshToken 비활성화 (로그아웃용)
//    @Modifying
//    @Query("UPDATE UserToken ut SET ut.isActive = false WHERE ut.refreshToken = :refreshToken")
//    void deactivateByRefreshToken(@Param("refreshToken") String refreshToken);
//
//    // 사용자의 모든 토큰 비활성화 (전체 로그아웃용)
//    @Modifying
//    @Query("UPDATE UserToken ut SET ut.isActive = false WHERE ut.user.id = :userId")
//    void deactivateAllTokensByUserId(@Param("userId") Long userId);
}
