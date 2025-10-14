package com.back.domain.funding.service;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.dto.request.FundingUpdateRequest;
import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.dto.response.FundingDetailResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingImage;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.FileType;
import com.back.global.s3.S3FileRequest;
import com.back.global.s3.S3Service;
import com.back.global.s3.S3ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundingService {

    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final S3ValidationService s3ValidationService;
    private final S3Service s3Service;
    private final FundingContributionRepository fundingContributionRepository;
    private final ArtistProfileRepository artistProfileRepository;

    @Transactional
    public Funding createFunding(FundingCreateRequest req, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ServiceException("400", "존재하지 않는 카테고리입니다."));

        // 엔티티 정적 팩토리로 생성(도메인 규칙 검증 포함)
        Funding funding = Funding.create(
                user,
                req.title(),
                req.description(),
                category,
                req.imageUrl(),
                req.targetAmount(),
                req.price(),
                req.stock(),
                req.startDate(),
                req.endDate(),
                FundingStatus.PENDING
        );

        if (req.images() != null && !req.images().isEmpty()) {
            var imgs = buildFundingImages(funding, req.images());
            funding.addImages(imgs);
        }

        return fundingRepository.save(funding);
    }

    @Transactional(readOnly = true)
    public FundingDetailResponse getFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다."));

        String artistDescription = artistProfileRepository
                .findByUserId(funding.getUser().getId())
                .map(ArtistProfile::getDescription)
                .orElse(null);


        // 누적 모금액, 참여자 수
        long currentAmount = nz(fundingContributionRepository.sumContributedAmountByFundingId(id));
        int participants = (int) nz(fundingContributionRepository.countDistinctParticipantsByFundingId(id));

        // 진행률
        double progress = funding.getTargetAmount() == 0
                ? 0d
                : Math.min(100d, (currentAmount * 100.0) / funding.getTargetAmount());

        // 남은 일수
        int remainingDays = 0;
        if (funding.getEndDate() != null && LocalDate.now() != null) {
            remainingDays = (int) ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    funding.getEndDate().toLocalDate()
            );
        }

        return new FundingDetailResponse(
                funding,
                currentAmount,
                participants,
                remainingDays,
                progress,
                artistDescription
        );
    }

    @Transactional(readOnly = true)
    public FundingImage getFundingDocument(Long fundingId) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다. fundingId: " + fundingId));

        return funding.getImages().stream()
                .filter(img -> img.getFileType() == FileType.DOCUMENT)
                .findFirst()
                .orElseThrow(() -> new ServiceException("404", "다운로드할 문서가 존재하지 않습니다."));
    }

    // null을 0L로 변환
    private long nz(Long v) {
        return v == null ? 0L : v;
    }

    @Transactional(readOnly = true)
    public Page<FundingCardDto> getFundingList(
            Set<FundingStatus> statuses,
            String sortBy,
            String keyword,
            Long categoryId,
            Long minPrice,
            Long maxPrice,
            int page,
            int size
    ) {
        // 정렬 생성
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Custom Repository 메서드 호출
        Page<Funding> fundingPage = fundingRepository.findByFilters(
                statuses, keyword, categoryId, minPrice, maxPrice, pageable
        );

        // DTO 변환
        return fundingPage.map(this::toCardDto);
    }

    private Sort createSort(String sortBy) {
        // null 처리 및 소문자 변환
        String safeSortBy = (sortBy != null) ? sortBy.trim().toLowerCase() : "recent";
        // 정렬 기준에 따른 Sort 객체 생성
        return switch (safeSortBy) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "participantCount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "endDate");
            case "recent" -> Sort.by(Sort.Direction.DESC, "createDate");
            case "highAmount" -> Sort.by(Sort.Direction.DESC, "targetAmount");
            default -> Sort.by(Sort.Direction.DESC, "createDate");
        };
    }

    public FundingCardDto toCardDto(Funding funding) {
        long currentAmount = funding.getCollectedAmount();
        double progress = (funding.getTargetAmount() > 0)
                ? (double) currentAmount / funding.getTargetAmount() * 100
                : 0;
        int remainingDays = (int) ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                funding.getEndDate()
        );

        return new FundingCardDto(funding, currentAmount, progress, remainingDays);
    }

    @Transactional
    public void updateFunding(Long FundingId, String userEmail, FundingUpdateRequest req) {
        Funding funding = fundingRepository.findById(FundingId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다."));

        if (!funding.getUser().getEmail().equals(userEmail)) {
            throw new ServiceException("403", "권한이 없습니다.");
        }

        if (req.title() != null || req.description() != null || req.imageUrl() != null) {
            funding.updateBasicInfo(req.title(), req.description(), req.imageUrl());
        }

        applyNumbersAndDates(funding, req);

    }

    private void applyNumbersAndDates(Funding f, FundingUpdateRequest req) {
        if (req.targetAmount() != null) {
            try { f.updateTargetAmount(req.targetAmount()); }
            catch (IllegalArgumentException | IllegalStateException e) { throw new ServiceException("400", e.getMessage()); }
        }
        if (req.price() != null) {
            try { f.updatePrice(req.price()); }
            catch (IllegalArgumentException | IllegalStateException e) { throw new ServiceException("400", e.getMessage()); }
        }
        if (req.stock() != null) {
            try { f.updateStock(req.stock()); }
            catch (IllegalArgumentException e) { throw new ServiceException("400", e.getMessage()); }
        }
        if (req.endDate() != null) {
            try { f.updateEndDate(req.endDate()); }
            catch (IllegalArgumentException | IllegalStateException e) { throw new ServiceException("400", e.getMessage()); }
        }
    }

    private List<FundingImage> buildFundingImages(Funding funding, List<S3FileRequest> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(img -> {
                    // S3에 파일이 존재하는지 검증
                    s3ValidationService.validateFileExists(img.s3Key());

                    return FundingImage.builder()
                            .funding(funding)
                            .fileUrl(img.url())
                            .fileType(img.type())
                            .s3Key(img.s3Key())
                            .originalFilename(img.originalFileName())
                            .build();
                }).toList();
    }

    private void updateFundingImages(Funding funding, List<S3FileRequest> incomingImages) {
        if (incomingImages == null) {
            incomingImages = List.of();
        }

        // 기존 이미지 맵
        Map<String, FundingImage> existingMap = funding.getImages().stream()
                .collect(Collectors.toMap(FundingImage::getS3Key, img -> img));

        // 프론트에게 받은 이미지 S3Key 리스트
        Set<String> incomingKeys = incomingImages.stream()
                .map(S3FileRequest::s3Key)
                .collect(Collectors.toSet());

        // DB, S3에서 이미지 삭제 (DB에 있고 프론트에 없는 이미지)
        funding.getImages().removeIf(img -> {
            if (!incomingKeys.contains(img.getS3Key())) {
                s3Service.deleteFile(img.getS3Key());
                return true;
            }
            return false;
        });

        // 이미지 추가 (프론트에 있는데 DB에는 없는 이미지)
        for (S3FileRequest img : incomingImages) {
            if (!existingMap.containsKey(img.s3Key())) {
                s3ValidationService.validateFileExists(img.s3Key());

                funding.getImages().add(FundingImage.builder()
                        .funding(funding)
                        .fileUrl(img.url())
                        .fileType(img.type())
                        .s3Key(img.s3Key())
                        .originalFilename(img.originalFileName())
                        .build());
            }
        }
    }

    @Transactional
    public void deleteFunding(Long fundingId, String userEmail) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다."));
        if (!funding.getUser().getEmail().equals(userEmail)) {
            throw new ServiceException("403", "권한이 없습니다.");
        }
        funding.delete();
    }
}
