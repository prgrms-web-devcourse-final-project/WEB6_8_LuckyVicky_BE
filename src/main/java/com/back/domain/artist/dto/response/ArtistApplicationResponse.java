package com.back.domain.artist.dto.response;


import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistDocument;
import com.back.domain.artist.entity.DocumentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 작가 신청서 조회 응답 DTO
 */
public record ArtistApplicationResponse(
        Long id,
        Long userId,
        String ownerName,
        String email,
        String phone,
        String artistName,
        String businessNumber,
        String businessName,
        String businessAddress,
        String businessAddressDetail,
        String businessZipCode,
        String telecomSalesNumber,
        String snsAccount,
        String mainProducts,
        String managerPhone,
        String bankName,
        String bankAccount,
        String accountName,
        ApplicationStatus status,
        String rejectionReason,
        String reviewedByName,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        Map<DocumentType, List<DocumentResponse>> documents
) {

    /**
     * Entity -> Response DTO 변환
     */
    public static ArtistApplicationResponse from(ArtistApplication application) {
        // 서류를 DocumentType별로 그룹화
        Map<DocumentType, List<DocumentResponse>> documentMap = application.getDocuments().stream()
                .collect(Collectors.groupingBy(
                        ArtistDocument::getDocumentType,
                        Collectors.mapping(DocumentResponse::from, Collectors.toList())
                ));

        return new ArtistApplicationResponse(
                application.getId(),
                application.getUser().getId(),
                application.getOwnerName(),
                application.getEmail(),
                application.getPhone(),
                application.getArtistName(),
                application.getBusinessNumber(),
                application.getBusinessName(),
                application.getBusinessAddress(),
                application.getBusinessAddressDetail(),
                application.getBusinessZipCode(),
                application.getTelecomSalesNumber(),
                application.getSnsAccount(),
                application.getMainProducts(),
                application.getManagerPhone(),
                application.getBankName(),
                application.getBankAccount(),
                application.getAccountName(),
                application.getStatus(),
                application.getRejectionReason(),
                application.getReviewedByName(),
                application.getReviewedAt(),
                application.getCreateDate(),
                documentMap
        );
    }


    /**
     * 서류 정보 DTO
     */
    public record DocumentResponse(
            Long id,
            DocumentType documentType,
            String fileName,
            String fileUrl,
            String fileExtension
    ) {
        public static DocumentResponse from(ArtistDocument document) {
            return new DocumentResponse(
                    document.getId(),
                    document.getDocumentType(),
                    document.getFileName(),
                    document.getFileUrl(),
                    document.getFileExtension()
            );
        }
    }

}
