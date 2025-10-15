package com.back.domain.funding.service;

import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingWish;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.funding.repository.FundingWishRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingWishService {

    private final FundingWishRepository fundingWishRepository;
    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;

    // 펀딩 찜 추가
    @Transactional
    public void addWish(Long fundingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다."));

        // 이미 찜했는지 확인
        if (fundingWishRepository.existsByUserIdAndFundingId(user.getId(), fundingId)) {
            throw new ServiceException("400", "이미 찜한 펀딩입니다.");
        }

        FundingWish wish = FundingWish.create(user, funding);
        fundingWishRepository.save(wish);
    }

    // 펀딩 찜 취소
    @Transactional
    public void removeWish(Long fundingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        FundingWish wish = fundingWishRepository.findByUserIdAndFundingId(user.getId(), fundingId)
                .orElseThrow(() -> new ServiceException("404", "찜하지 않은 펀딩입니다."));

        fundingWishRepository.delete(wish);
    }

    // 찜 여부 확인
    @Transactional(readOnly = true)
    public boolean isWished(Long fundingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        return fundingWishRepository.existsByUserIdAndFundingId(user.getId(), fundingId);
    }

    // 사용자의 찜 목록 조회
    @Transactional(readOnly = true)
    public Page<FundingCardDto> getMyWishList(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        Page<FundingWish> wishPage = fundingWishRepository.findByUserIdWithFunding(
                user.getId(), pageable
        );

        return wishPage.map(wish -> {
            Funding funding = wish.getFunding();
            // FundingService의 toCardDto 로직 재사용
            return toCardDto(funding);
        });
    }

    private FundingCardDto toCardDto(Funding funding) {
        long currentAmount = funding.getCollectedAmount();
        double progress = (funding.getTargetAmount() > 0)
                ? (double) currentAmount / funding.getTargetAmount() * 100
                : 0;
        int remainingDays = (int) java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDateTime.now(),
                funding.getEndDate()
        );

        return new FundingCardDto(funding, currentAmount, progress, remainingDays);
    }
}
