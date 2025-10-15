package com.back.domain.artist.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 작가 신청서 생성 요청 DTO
 */
public record ArtistApplicationRequest(
        @NotBlank(message = "대표자명(실명)은 필수입니다.")
        @Size(max = 20, message = "대표자명(실명)은 최대 20자까지 가능합니다.")
        String ownerName,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 100, message = "이메일은 최대 100자까지 가능합니다.")
        String email,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 20, message = "전화번호는 최대 20자까지 가능합니다.")
        String phone,

        @NotBlank(message = "작가명은 필수입니다.")
        @Size(max = 20, message = "작가명은 최대 20자까지 가능합니다.")
        String artistName,

        @NotBlank(message = "사업자등록번호는 필수입니다.")
        String businessNumber,

        @NotBlank(message = "사업장 주소는 필수입니다.")
        String businessAddress,

        @NotBlank(message = "상세 주소는 필수입니다.")
        String businessAddressDetail,

        @NotBlank(message = "우편번호는 필수입니다.")
        String businessZipCode,

        @NotBlank(message = "통신판매업 신고번호는 필수입니다.")
        String telecomSalesNumber,

        // ==== 선택 필드 ==== //
        String businessName,        // 상호명 (선택)
        String snsAccount,          // SNS 계정 (선택)
        String mainProducts,        // 주요 취급 상품 (선택)
        String managerPhone,        // 담당자 연락처 (선택)
        String bankName,            // 은행명 (선택)
        String bankAccount,         // 계좌번호 (선택)
        String accountName          // 예금주명 (선택)

) {
}
