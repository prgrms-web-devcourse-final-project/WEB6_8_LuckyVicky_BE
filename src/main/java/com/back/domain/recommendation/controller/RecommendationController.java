package com.back.domain.recommendation.controller;

import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.recommendation.dto.request.PreferenceRequest;
import com.back.domain.recommendation.dto.response.MatchResponse;
import com.back.domain.recommendation.dto.response.RecommendedItem;
import com.back.domain.recommendation.service.MatcherService;
import com.back.domain.recommendation.service.TagDictionary;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 취향 기반 상품 추천 컨트롤러
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "상품 추천", description = "취향 테스트 기반 상품 추천 API")
public class RecommendationController {
    
    private final ProductRepository productRepository;
    private final TagDictionary tagDictionary;
    private final MatcherService matcherService;
    
    @PostMapping("/match")
    @Operation(
            summary = "취향 기반 상품 매칭",
            description = "사용자의 취향 테스트 결과(태그별 점수)를 기반으로 상품을 추천합니다. " +
                         "7~9문항의 테스트 결과를 태그별 점수로 변환하여 요청하세요.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "추천 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "200",
                                                      "msg": "5개 상품을 추천했습니다",
                                                      "data": {
                                                        "recommendations": [
                                                          {
                                                            "rank": 1,
                                                            "matchScore": 2.4,
                                                            "product": {
                                                              "productUuid": "550e8400-...",
                                                              "imageUrl": "https://...",
                                                              "brandName": "문구브랜드",
                                                              "name": "부드러운 4B 연필",
                                                              "price": 15000,
                                                              "discountRate": 10,
                                                              "discountPrice": 13500,
                                                              "rating": 4.5
                                                            },
                                                            "matchedTags": [
                                                              {"name": "부드러운", "yourScore": 0.9},
                                                              {"name": "실용적인", "yourScore": 0.8}
                                                            ],
                                                            "reason": "'부드러운·실용적인·데일리' 선호와 일치"
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<MatchResponse>> matchProducts(
            @Valid @RequestBody PreferenceRequest request) {

        try {
            log.info("===== 추천 요청 시작 =====");
            log.info("선호 태그: {}", request.preferences());
            log.info("가격: {}-{}", request.minPrice(), request.maxPrice());

            // 1. 입력값 유효성 검증
            if (request.preferences() == null || request.preferences().isEmpty()) {
                log.warn("선호 태그가 비어있음");
                return ResponseEntity.ok(RsData.of("400",
                        "선호 태그 정보가 필요합니다",
                        new MatchResponse(List.of())));
            }

            // 가격 범위 검증
            int minPrice = Optional.ofNullable(request.minPrice()).orElse(0);
            int maxPrice = Optional.ofNullable(request.maxPrice()).orElse(9_999_999);

            if (minPrice < 0 || maxPrice < 0) {
                log.warn("잘못된 가격 범위: min={}, max={}", minPrice, maxPrice);
                return ResponseEntity.ok(RsData.of("400",
                        "가격은 0 이상이어야 합니다",
                        new MatchResponse(List.of())));
            }

            if (minPrice > maxPrice) {
                log.warn("최소 가격이 최대 가격보다 큼: min={}, max={}", minPrice, maxPrice);
                return ResponseEntity.ok(RsData.of("400",
                        "최소 가격은 최대 가격보다 클 수 없습니다",
                        new MatchResponse(List.of())));
            }

            // 2. 상위 선호 태그 추출 (점수 0.3 이상)
            List<String> topTagNames = request.preferences().entrySet().stream()
                    .filter(entry -> entry.getValue() >= 0.3)  // 0.5 → 0.3으로 낮춤
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList();

            log.info("선호도 0.3 이상 태그: {}", topTagNames);

            if (topTagNames.isEmpty()) {
                log.warn("선호도 0.3 이상인 태그가 없음");
                return ResponseEntity.ok(RsData.of("200",
                        "선호도가 충분히 높은 태그가 없습니다. 테스트를 다시 진행해주세요.",
                        new MatchResponse(List.of())));
            }

            // 3. 태그명 → 태그ID 변환
            List<Long> tagIds;
            try {
                tagIds = tagDictionary.toIds(topTagNames);
                log.info("변환된 태그 ID: {}", tagIds);
            } catch (IllegalArgumentException e) {
                log.error("태그 변환 중 오류: {}", e.getMessage());
                return ResponseEntity.ok(RsData.of("400",
                        "잘못된 태그 정보입니다: " + e.getMessage(),
                        new MatchResponse(List.of())));
            } catch (Exception e) {
                log.error("태그 변환 중 예상치 못한 오류", e);
                return ResponseEntity.ok(RsData.of("500",
                        "태그 처리 중 오류가 발생했습니다",
                        new MatchResponse(List.of())));
            }

            if (tagIds.isEmpty()) {
                log.warn("유효한 태그 ID를 찾을 수 없음. 입력된 태그명: {}", topTagNames);
                return ResponseEntity.ok(RsData.of("200",
                        "해당하는 상품 태그를 찾을 수 없습니다",
                        new MatchResponse(List.of())));
            }

            // 4. 후보 상품 조회 (충분한 후보군 확보 후 상위 3개 선택)
            int page = 0; // 첫 페이지만
            int size = 50; // 50개 후보 조회 → 점수 계산 → 상위 3개 선택

            log.info("후보군 조회: {}개 (이 중 최적 3개 선택)", size);
            log.info("가격 필터: {}원 ~ {}원", minPrice, maxPrice);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));

            // 기존 findProducts 메서드 활용 (태그 필터링 포함)
            ProductListResponse response;
            try {
                response = productRepository.findProducts(
                        null,           // categoryId: 전체 카테고리
                        tagIds,         // 선호 태그로 필터링
                        minPrice,       // 최소 가격
                        maxPrice,       // 최대 가격
                        null,           // deliveryType: 전체
                        "newest",       // 신상품순
                        pageable
                );
                log.info("findProducts 결과: {}개 상품", response.products().size());
            } catch (Exception e) {
                log.error("상품 조회 중 오류 발생", e);
                return ResponseEntity.ok(RsData.of("500",
                        "상품 조회 중 오류가 발생했습니다",
                        new MatchResponse(List.of())));
            }

            if (response.products().isEmpty()) {
                log.warn("조건에 맞는 상품이 없음 (findProducts 단계)");
                return ResponseEntity.ok(RsData.of("200",
                        "조건에 맞는 상품이 없습니다. 가격 범위를 조정해보세요.",
                        new MatchResponse(List.of())));
            }

            // ProductInfo → Product 변환이 필요하므로, UUID로 다시 조회 (태그 포함)
            List<Product> filtered;
            try {
                filtered = response.products().stream()
                        .map(productInfo -> {
                            try {
                                Optional<Product> product = productRepository.findByProductUuidWithTags(productInfo.productUuid());
                                if (product.isEmpty()) {
                                    log.warn("상품 UUID로 조회 실패: {}", productInfo.productUuid());
                                }
                                return product;
                            } catch (Exception e) {
                                log.error("상품 UUID 조회 중 오류: {}", productInfo.productUuid(), e);
                                return Optional.<Product>empty();
                            }
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

                log.info("UUID 재조회 결과: {}개 상품", filtered.size());
            } catch (Exception e) {
                log.error("상품 상세 조회 중 오류 발생", e);
                return ResponseEntity.ok(RsData.of("500",
                        "상품 상세 정보 조회 중 오류가 발생했습니다",
                        new MatchResponse(List.of())));
            }

            if (filtered.isEmpty()) {
                log.warn("조건에 맞는 상품이 없음 (UUID 재조회 단계)");
                return ResponseEntity.ok(RsData.of("200",
                        "조건에 맞는 상품이 없습니다",
                        new MatchResponse(List.of())));
            }

            // 5. 매칭 스코어 계산 및 정렬
            List<RecommendedItem> ranked;
            try {
                log.info("매칭 스코어 계산 시작...");
                ranked = matcherService.scoreAndRank(
                        filtered,
                        request.preferences(),
                        request.specPrefs()
                );
                log.info("✅ 매칭 스코어 계산 완료: {}개", ranked.size());
            } catch (IllegalArgumentException e) {
                log.error("❌ 매칭 스코어 계산 중 잘못된 입력: {}", e.getMessage());
                return ResponseEntity.ok(RsData.of("400",
                        "추천 계산 중 오류: " + e.getMessage(),
                        new MatchResponse(List.of())));
            } catch (NullPointerException e) {
                log.error("❌ 매칭 스코어 계산 중 null 값 발견", e);
                return ResponseEntity.ok(RsData.of("500",
                        "추천 계산 중 데이터 오류가 발생했습니다",
                        new MatchResponse(List.of())));
            } catch (Exception e) {
                log.error("❌ 매칭 스코어 계산 중 오류 발생", e);
                return ResponseEntity.ok(RsData.of("500",
                        "추천 계산 중 오류가 발생했습니다",
                        new MatchResponse(List.of())));
            }

            if (ranked == null || ranked.isEmpty()) {
                log.warn("매칭 스코어 계산 결과가 비어있음");
                return ResponseEntity.ok(RsData.of("200",
                        "추천할 수 있는 상품이 없습니다",
                        new MatchResponse(List.of())));
            }

            // 6. 상위 3개만 추출 (프론트 요구사항)
            final int RECOMMENDATION_LIMIT = 3;
            List<RecommendedItem> topN = ranked.stream()
                    .limit(RECOMMENDATION_LIMIT)
                    .toList();

            log.info("===== 최종 추천: {}개 상품 (고정) =====", topN.size());

            MatchResponse matchResponse = new MatchResponse(topN);
            RsData<MatchResponse> result = RsData.of("200",
                    topN.size() + "개 상품을 추천했습니다",
                    matchResponse);

            log.info("🎉 응답 반환 준비 완료");
            log.info("Response: resultCode={}, msg={}, data.size={}",
                    result.resultCode(), result.msg(), topN.size());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("❌ 잘못된 입력값 오류", e);
            return ResponseEntity.ok(RsData.of("400",
                    "잘못된 요청입니다: " + e.getMessage(),
                    new MatchResponse(List.of())));
        } catch (NullPointerException e) {
            log.error("❌ Null 값 참조 오류", e);
            return ResponseEntity.ok(RsData.of("500",
                    "데이터 처리 중 오류가 발생했습니다",
                    new MatchResponse(List.of())));
        } catch (Exception e) {
            log.error("❌❌❌ RecommendationController에서 예상치 못한 오류 발생 ❌❌❌", e);
            log.error("오류 타입: {}", e.getClass().getName());
            log.error("오류 메시지: {}", e.getMessage());

            return ResponseEntity.ok(RsData.of("500",
                    "추천 시스템에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    new MatchResponse(List.of())));
        }
    }
}
