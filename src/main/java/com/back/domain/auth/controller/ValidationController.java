package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.ValidationCheckRequest;
import com.back.domain.auth.dto.response.ValidationCheckResponse;
import com.back.domain.auth.service.ValidationService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Duplicate Validation", description = "중복 검증 API")
@RestController
@RequestMapping("/api/auth/duplicate")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/email")
    @Operation(summary = "이메일 중복 검사", description = "이메일 중복 여부를 확인합니다.")
    public ResponseEntity<RsData<ValidationCheckResponse>> checkEmailDuplicate(
            @Valid @RequestBody ValidationCheckRequest request
    ) {
        ValidationCheckResponse response = validationService.checkEmailDuplication(request.value());

        return ResponseEntity.ok(
                RsData.of("200", "이메일 중복 검사 완료", response)
        );
    }

    @PostMapping("/name")
    @Operation(summary = "닉네임 중복 검사", description = "닉네임 중복 여부를 확인합니다.")
    public ResponseEntity<RsData<ValidationCheckResponse>> checkNameDuplicate(
            @Valid @RequestBody ValidationCheckRequest request
    ) {
        ValidationCheckResponse response = validationService.checkNameDuplicate(request.value());

        return ResponseEntity.ok(
                RsData.of("200", "닉네임 중복 검사 완료", response)
        );
    }

    @PostMapping("/phone")
    @Operation(summary = "전화번호 중복 검사", description = "전화번호 중복 여부를 확인합니다.")
    public ResponseEntity<RsData<ValidationCheckResponse>> checkPhoneDuplicate(
            @Valid @RequestBody ValidationCheckRequest request
    ) {
        ValidationCheckResponse response = validationService.checkPhoneDuplicate(request.value());

        return ResponseEntity.ok(
                RsData.of("200", "전화번호 중복 검사 완료", response)
        );
    }
}
