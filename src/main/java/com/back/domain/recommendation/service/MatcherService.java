package com.back.domain.recommendation.service;

import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.entity.Product;
import com.back.domain.recommendation.dto.response.MatchedTag;
import com.back.domain.recommendation.dto.response.RecommendedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 상품 매칭 스코어 계산 및 정렬 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MatcherService {
    
    private final OpenAIRecommendationService openAIService;
    
    private static final double TAG_WEIGHT = 1.0;
    
    /**
     * 상품 리스트에 대해 매칭 스코어 계산 후 정렬
     */
    public List<RecommendedItem> scoreAndRank(
            List<Product> products,
            Map<String, Double> preferences,
            Map<String, Double> specPrefs // nullable (Phase 2에서 사용)
    ) {
        log.info("매칭 스코어 계산 시작 - 후보 상품: {}개, 선호 태그: {}개", 
                products.size(), preferences.size());
        
        List<ScoredProduct> scored = new ArrayList<>();
        
        // OpenAI는 상위 3개 상품에만 사용 (성능 최적화)
        int openAICount = 0;
        final int MAX_OPENAI_CALLS = 3; // 분당 Rate Limit 고려
        
        for (Product product : products) {
            // 1. 태그 점수 계산
            double tagScore = product.getProductTags().stream()
                    .map(mapping -> {
                        String tagName = mapping.getTag().getName();
                        return preferences.getOrDefault(tagName, 0.0) * TAG_WEIGHT;
                    })
                    .reduce(0.0, Double::sum);
            
            // 2. 스펙 점수 계산 (Phase 2)
            double specScore = 0.0;
            // TODO: Product에 specs 필드 추가 후 구현
            // if (specPrefs != null && !specPrefs.isEmpty()) { ... }
            
            double totalScore = tagScore + specScore;
            
            // 3. 매칭된 태그 목록
            List<MatchedTag> matchedTags = product.getProductTags().stream()
                    .map(mapping -> mapping.getTag().getName())
                    .filter(preferences::containsKey)
                    .map(tagName -> new MatchedTag(tagName, preferences.get(tagName)))
                    .sorted(Comparator.comparingDouble(MatchedTag::yourScore).reversed())
                    .toList();
            
            // 4. 추천 이유 생성 (OpenAI는 상위 N개 상품에만 사용)
            String reason;
            if (openAICount < MAX_OPENAI_CALLS) {
                try {
                    // Phase 2: LLM 기반 추천 이유
                    reason = openAIService.generateRecommendationReason(
                        product, 
                        preferences, 
                        totalScore
                    );
                    openAICount++;
                } catch (Exception e) {
                    log.debug("OpenAI 추천 이유 생성 실패, 기본 로직 사용: {}", e.getMessage());
                    reason = buildReason(matchedTags);
                }
            } else {
                reason = buildReason(matchedTags);
            }
            
            scored.add(new ScoredProduct(product, totalScore, matchedTags, reason));
        }
        
        // 점수 내림차순 정렬
        scored.sort(Comparator.comparingDouble(ScoredProduct::score).reversed());
        
        // RecommendedItem으로 변환 (rank 추가)
        List<RecommendedItem> result = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            ScoredProduct sp = scored.get(i);
            result.add(new RecommendedItem(
                    i + 1,
                    round2(sp.score),
                    toProductInfo(sp.product),
                    sp.matchedTags,
                    sp.reason
            ));
        }
        
        log.info("매칭 스코어 계산 완료 - 최고 점수: {}", 
                result.isEmpty() ? 0 : result.getFirst().matchScore());
        
        return result;
    }
    
    /**
     * 추천 이유 문장 생성 (기본 로직)
     */
    private String buildReason(List<MatchedTag> matchedTags) {
        if (matchedTags.isEmpty()) {
            return "비슷한 스타일의 상품이에요";
        }
        
        String tagNames = matchedTags.stream()
                .limit(3)
                .map(MatchedTag::name)
                .collect(Collectors.joining("·"));
        
        return "'" + tagNames + "' 선호와 일치";
    }
    
    /**
     * Product → ProductInfo 변환
     */
    private ProductListResponse.ProductInfo toProductInfo(Product product) {
        String mainImageUrl = product.getImages().stream()
                .filter(img -> img.getFileType() == com.back.global.s3.FileType.MAIN)
                .findFirst()
                .map(img -> img.getFileUrl())
                .orElse(null);
        
        return new ProductListResponse.ProductInfo(
                product.getProductUuid(),
                mainImageUrl,
                product.getBrandName(),
                product.getName(),
                product.getPrice(),
                product.getDiscountRate(),
                product.getDiscountPrice(),
                product.getAverageRating() != null ? product.getAverageRating() : 0.0
        );
    }
    
    /**
     * 소수점 둘째자리 반올림
     */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    
    /**
     * 내부 사용: 점수가 포함된 상품
     */
    private record ScoredProduct(
            Product product,
            double score,
            List<MatchedTag> matchedTags,
            String reason
    ) {}
}
