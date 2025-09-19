package com.back.domain.dashboard.dto.response;

import com.back.global.util.PageResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 작가 신청 관련 응답 DTO
 */
public class ArtistApplicationResponse {
    
    @Getter
    @Setter
    public static class List extends PageResponse<ArtistApplicationResponse.Summary> {
        private SummaryDto summary;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Summary> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Application application;
        private Applicant applicant;
        private Contact contact;
        private Business business;
        private Profile profile;
        private Permission permissions;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private int total;
        private int pending;
        private int approved;
        private int rejected;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long applicationId;
        private String artistName;
        private String submittedAt;
        private String status;
        private String statusText;
        private Permission permissions;
        private LocalDateTime lastUpdatedAt;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Application {
        private Long applicationId;
        private String status;
        private String statusText;
        private LocalDateTime submittedAt;
        private LocalDateTime decidedAt;
        private String rejectionReason;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Applicant {
        private String memberId;
        private String artistName;
        private String avatarUrl;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private String email;
        private String phone;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Business {
        private String registrationNo;
        private FileDto registrationProof;
        private String telemarketingReportNo;
        private FileDto telemarketingReportProof;
        private String address;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String description;
        private java.util.List<String> mainCategories;
        private java.util.List<Sns> sns;
        private java.util.List<FileDto> portfolio;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDto {
        private String fileId;
        private String fileName;
        private String url;
        private LocalDateTime expiresAt;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sns {
        private String platform;
        private String handle;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private Boolean canEdit;
        private Boolean canCancel;
    }
}
