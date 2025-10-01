package com.back.domain.auth.dto.response;

public record SignUpResponse(
        Long userId,
        String email,
        String name
) {}
