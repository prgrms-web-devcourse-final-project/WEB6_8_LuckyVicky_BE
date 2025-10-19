package com.back.domain.artist.repository;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.entity.QArtistProfile;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArtistProfileCustomRepositoryImpl implements ArtistProfileCustomRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 검색 키워드(작가 이름)에 해당하는 작가 조회
     */
    @Override
    public List<ArtistProfile> searchByArtistName(String keyword) {
        QArtistProfile artistProfile = QArtistProfile.artistProfile;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(keyword)) {
            String[] keywords = keyword.trim().toLowerCase().split("\\s+"); // 키워드 앞뒤 공백 제거, 소문자 변환, 공백 기준으로 분리
            for (String kw : keywords) {
                builder.or(artistProfile.artistName.toLowerCase().contains(kw)); // or 조건(여러 키워드 중 하나라도 해당하면 조회)
            }
        }

        return queryFactory
                .selectFrom(artistProfile)
                .where(builder)
                .fetch();
    }
}