package com.back.domain.recommendation.service;

import com.back.domain.product.tag.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 태그명 → 태그ID 변환 캐시
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TagDictionary {
    
    private final TagRepository tagRepository;
    private final Map<String, Long> nameToIdCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    void loadTags() {
        List<com.back.domain.product.tag.entity.Tag> allTags = tagRepository.findAll();
        log.info("===== 태그 로드 시작 =====");
        log.info("DB에서 조회된 태그 수: {}", allTags.size());
        
        allTags.forEach(tag -> {
            nameToIdCache.put(tag.getName(), tag.getId());
            log.info("태그 등록: {} -> ID:{}", tag.getName(), tag.getId());
        });
        
        log.info("===== 태그 사전 로드 완료: {} 개 =====", nameToIdCache.size());
        log.info("등록된 태그 목록: {}", nameToIdCache.keySet());
    }
    
    /**
     * 태그명 리스트를 태그 ID 리스트로 변환
     */
    public List<Long> toIds(List<String> tagNames) {
        return tagNames.stream()
                .map(name -> nameToIdCache.getOrDefault(name, -1L))
                .filter(id -> id > 0)
                .toList();
    }
    
    /**
     * 캐시 갱신 (관리자가 태그 추가 시 호출)
     */
    public void refresh() {
        nameToIdCache.clear();
        loadTags();
    }
}
