package com.back.domain.user.repository;

import com.back.domain.user.entity.Provider;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
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
}
