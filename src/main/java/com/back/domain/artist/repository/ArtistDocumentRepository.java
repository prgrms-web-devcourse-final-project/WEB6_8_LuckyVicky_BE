package com.back.domain.artist.repository;

import com.back.domain.artist.entity.ArtistDocument;
import com.back.domain.artist.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistDocumentRepository extends JpaRepository<ArtistDocument, Long> {

    /**
     * 작가 신청서의 모든 서류 조회
     */
    List<ArtistDocument> findByArtistApplicationId(Long applicationId);

    /**
     * 작가 신청서의 특정 타입 서류 조회
     */
    List<ArtistDocument> findByArtistApplicationIdAndDocumentType(
            Long applicationId,
            DocumentType documentType
    );

    /**
     * 작가 신청서의 서류 존재 여부
     */
    boolean existsByArtistApplicationId(Long applicationId);

    /**
     * 작가 신청서의 서류 개수 조회
     */
    long countByArtistApplicationId(Long applicationId);
}
