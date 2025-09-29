package com.back.domain.funding.service;

import com.back.domain.funding.dto.request.FundingNewsCreateRequest;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingNews;
import com.back.domain.funding.repository.FundingNewsRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingNewsService {
    private final FundingNewsRepository fundingNewsRepository;
    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long addFundingNews(Long fundingId, FundingNewsCreateRequest req, String userEmail) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다."));

        User artist = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("403", "존재하지 않는 사용자입니다."));

        if (!funding.getUser().getId().equals(artist.getId())) {
            throw new ServiceException("403", "작성자만 새소식을 등록할 수 있습니다.");
        }

        FundingNews news = FundingNews.builder()
                .funding(funding)
                .artist(artist)
                .title(req.title())
                .content(req.content())
                .imageUrl(req.imageUrl())
                .build();

        fundingNewsRepository.save(news);
        return news.getId();
    }
}