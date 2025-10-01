package com.back.domain.auth.service;

import com.back.domain.auth.dto.response.ValidationCheckResponse;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValidationService {

    private final UserRepository userRepository;

    /**
     * 이메일 중복 검사
     */
    public ValidationCheckResponse checkEmailDuplication(String email) {
        boolean isDuplicate = userRepository.existsByEmail(email);

        log.debug("이메일 중복 검사: email={}, isDuplicate={}", email, isDuplicate);

        return ValidationCheckResponse.of(
                email,
                "email",
                isDuplicate,
                isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다."
        );
    }

    /**
     * 닉네임 중복 검사
     */
    public ValidationCheckResponse checkNameDuplicate(String name) {
        boolean isDuplicate = userRepository.existsByName(name);

        log.debug("닉네임 중복 검사: name={}, isDuplicate={}", name, isDuplicate);

        return ValidationCheckResponse.of(
                name,
                "name",
                isDuplicate,
                isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다."
        );
    }


    /**
     * 전화번호 중복 검사
     */
    public ValidationCheckResponse checkPhoneDuplicate(String phone) {
        boolean isDuplicate = userRepository.existsByPhone(phone);

        log.debug("전화번호 중복 검사: phone={}, isDuplicate={}", phone, isDuplicate);

        return ValidationCheckResponse.of(
                phone,
                "phone",
                isDuplicate,
                isDuplicate ? "이미 사용 중인 전화번호입니다." : "사용 가능한 전화번호입니다."
        );
    }


    /**
     * 특정 사용자 ID를 제외한 중복 검사 (회원정보 수정시 사용)
     * 필요시 주석 해제 후 사용
     */
    /*
    public ValidationCheckResponse checkEmailDuplicateExcludingUserId(String email, Long excludeUserId) {
        boolean isDuplicate = userRepository.existsByEmailAndIdNot(email, excludeUserId);

        log.debug("이메일 중복 검사 (사용자 제외): email={}, excludeUserId={}, isDuplicate={}",
                email, excludeUserId, isDuplicate);

        return ValidationCheckResponse.of(
                email,
                "email",
                isDuplicate,
                isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다."
        );
    }

    public ValidationCheckResponse checkNameDuplicateExcludingUserId(String name, Long excludeUserId) {
        boolean isDuplicate = userRepository.existsByNameAndIdNot(name, excludeUserId);

        log.debug("닉네임 중복 검사 (사용자 제외): name={}, excludeUserId={}, isDuplicate={}",
                name, excludeUserId, isDuplicate);

        return ValidationCheckResponse.of(
                name,
                "name",
                isDuplicate,
                isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다."
        );
    }

    public ValidationCheckResponse checkPhoneDuplicateExcludingUserId(String phone, Long excludeUserId) {
        boolean isDuplicate = userRepository.existsByPhoneAndIdNot(phone, excludeUserId);

        log.debug("전화번호 중복 검사 (사용자 제외): phone={}, excludeUserId={}, isDuplicate={}",
                phone, excludeUserId, isDuplicate);

        return ValidationCheckResponse.of(
                phone,
                "phone",
                isDuplicate,
                isDuplicate ? "이미 사용 중인 전화번호입니다." : "사용 가능한 전화번호입니다."
        );
    }
    */
}
