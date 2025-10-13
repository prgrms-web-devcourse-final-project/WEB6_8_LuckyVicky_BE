package com.back.domain.artist.repository;

import com.back.domain.artist.entity.ArtistProfile;

import java.util.List;

public interface ArtistProfileCustomRepository {

    // 검색(작가)
    List<ArtistProfile> searchByArtistName(String keyword);
}
