package com.back.domain.recommendation.service;

import com.back.domain.product.product.entity.Product;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * OpenAI API를 사용한 고급 추천 서비스
 * Phase 2: LLM 기반 자연어 추천 이유 생성
 */
@Service
@Slf4j
public class OpenAIRecommendationService {
    
    private final OpenAIClient openAIClient;
    private final boolean isEnabled;
    
    public OpenAIRecommendationService(@Value("${openai.api-key:}") String apiKey) {
        OpenAIClient client = null;
        boolean enabled = false;
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ OPENAI_API_KEY가 설정되지 않았습니다. 기본 추천 로직을 사용합니다.");
            log.warn("   활성화하려면:");
            log.warn("   1. .env 파일에 OPENAI_API_KEY 추가");
            log.warn("   2. ./gradlew clean build 실행");
            log.warn("   3. 서버 재시작");
        } else {
            try {
                client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
                enabled = true;
                log.info("✅ OpenAI 추천 서비스 활성화 완료 (gpt-4o-mini)");
            } catch (Exception e) {
                log.error("❌ OpenAI 클라이언트 초기화 실패: {}", e.getMessage());
            }
        }
        
        this.openAIClient = client;
        this.isEnabled = enabled;
    }
    
    /**
     * LLM을 사용한 추천 이유 생성 (OpenAI 우선, 실패 시 fallback)
     */
    public String generateRecommendationReason(
            Product product,
            Map<String, Double> userPreferences,
            double matchScore
    ) {
        // OpenAI 비활성화 시 기본 로직 사용
        if (!isEnabled || openAIClient == null) {
            log.debug("OpenAI 비활성화 상태, 기본 로직 사용");
            return generateFallbackReason(product, userPreferences);
        }
        
        try {
            String prompt = buildPrompt(product, userPreferences, matchScore);
            
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addUserMessage("당신은 문구/디자인 상품 추천 전문가입니다. " +
                        "다음 정보를 바탕으로 이 상품을 추천하는 이유를 30자 이내로 작성하세요.\n\n" +
                        prompt)
                .maxCompletionTokens(100L)
                .temperature(0.7)
                .build();
            
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            
            String response = completion.choices().stream()
                .findFirst()
                .flatMap(choice -> choice.message().content())
                .orElse("");
            
            if (response.isBlank()) {
                log.warn("⚠️ OpenAI 응답이 비어있음, 기본 로직 사용");
                return generateFallbackReason(product, userPreferences);
            }
            
            log.info("✨ GPT 추천 이유 생성 성공: {}", response);
            return response.trim();
            
        } catch (Exception e) {
            // Rate Limit 포함 모든 에러 처리
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                log.debug("⏱️ OpenAI Rate Limit, 기본 로직 사용");
            } else {
                log.warn("⚠️ OpenAI API 호출 실패, 기본 로직 사용: {}", e.getMessage());
            }
            return generateFallbackReason(product, userPreferences);
        }
    }
    
    /**
     * 프롬프트 생성
     */
    private String buildPrompt(Product product, Map<String, Double> userPreferences, double matchScore) {
        String topPrefs = userPreferences.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .reduce((a, b) -> a + ", " + b)
            .orElse("없음");
        
        String productTags = product.getProductTags().stream()
            .map(mapping -> mapping.getTag().getName())
            .reduce((a, b) -> a + ", " + b)
            .orElse("없음");
        
        return String.format(
            """
            상품명: %s
            브랜드: %s
            상품 태그: %s
            사용자 선호: %s
            매칭 점수: %.2f
            
            위 정보를 바탕으로 이 상품을 추천하는 이유를 30자 이내로 작성하세요.
            """,
            product.getName(),
            product.getBrandName(),
            productTags,
            topPrefs,
            matchScore
        );
    }
    
    /**
     * 기본 추천 이유 생성 로직 (OpenAI 없이 동작)
     */
    private String generateFallbackReason(Product product, Map<String, Double> userPreferences) {
        List<String> matchedTags = product.getProductTags().stream()
            .map(mapping -> mapping.getTag().getName())
            .filter(userPreferences::containsKey)
            .limit(3)
            .toList();
        
        if (matchedTags.isEmpty()) {
            return "비슷한 스타일의 상품이에요";
        }
        
        String tagNames = String.join("·", matchedTags);
        return "'" + tagNames + "' 선호와 일치";
    }
}
