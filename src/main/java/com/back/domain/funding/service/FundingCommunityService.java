package com.back.domain.funding.service;

import com.back.domain.funding.dto.request.FundingCommunityCreateRequest;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingCommunity;
import com.back.domain.funding.repository.FundingCommunityRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingCommunityService {

    private final FundingRepository fundingRepository;
    private final FundingCommunityRepository fundingCommunityRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long fundingId, FundingCommunityCreateRequest req, String username) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new ServiceException("404", "펀딩을 찾을 수 없습니다."));
        User author = userRepository.findByEmail(username)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 사용자입니다."));
        FundingCommunity post = FundingCommunity.create(funding, author, req.content());
        fundingCommunityRepository.save(post);
        return post.getId();
    }

    @Transactional
    public void delete(Long fundingId, Long communityId, String username) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new ServiceException("404", "펀딩을 찾을 수 없습니다."));
        FundingCommunity post = fundingCommunityRepository.findById(communityId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 글입니다."));
        User author = userRepository.findByEmail(username)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 사용자입니다."));
        boolean isAuthor = post.getAuthor().getId().equals(author.getId());
        boolean isFundingOwner = funding.getUser().getId().equals(author.getId());
        boolean isAdmin = author.getRole() == Role.ADMIN;

        if (!(isAuthor || isFundingOwner || isAdmin)) {
            throw new ServiceException("403", "권한이 없습니다.");
        }
        post.delete();
    }
}