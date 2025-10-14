package com.back.domain.funding.service;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.dto.request.FundingUpdateRequest;
import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.dto.response.FundingDetailResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.S3ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundingService {

    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final S3ValidationService s3ValidationService;
    private final FundingContributionRepository fundingContributionRepository;
    private final ArtistProfileRepository artistProfileRepository;

    @Transactional
    public Funding createFunding(FundingCreateRequest req, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."));

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

        if (req.targetAmount() != null) {
            try {
                funding.updateTargetAmount(req.targetAmount());
            } catch (IllegalStateException e) {
                throw new ServiceException("400", e.getMessage());
            }
        }

        if (req.price() != null) {
            try {
                funding.updatePrice(req.price());
            } catch (IllegalStateException e) {
                throw new ServiceException("400", e.getMessage());
            }
        }

        if (req.stock() != null) {
            try {
                funding.updateStock(req.stock());
            } catch (IllegalArgumentException e) {
                throw new ServiceException("400", e.getMessage());
            }
        }

        if (req.endDate() != null) {
            try {
                funding.updateEndDate(req.endDate());
            } catch (IllegalStateException e) {
                throw new ServiceException("400", e.getMessage());
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
