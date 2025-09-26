package com.back.domain.funding.service;

import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingOption;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingOptionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.S3ValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundingService {

    private final FundingRepository fundingRepository;
    private final FundingOptionRepository fundingOptionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final S3ValidationService s3ValidationService;

    @Transactional
    public Funding createFunding(FundingCreateRequest req, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

//        Category category = categoryRepository.findById(req.categoryId())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."));

        List<FundingOption> optionEntities = req.options().stream()
                .map(o -> FundingOption.create(o.name(), o.price(), o.stock(), o.sortOrder()))
                .toList();

        // 엔티티 정적 팩토리로 생성(도메인 규칙 검증 포함)
        Funding funding = Funding.create(
                user,
                req.title(),
                req.description(),
//                category,
                req.imageUrl(),
                req.targetAmount(),
                req.startDate(),
                req.endDate(),
                FundingStatus.OPEN,
                optionEntities
        );

        return fundingRepository.save(funding);
    }
}
