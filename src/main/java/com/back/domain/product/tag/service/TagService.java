package com.back.domain.product.tag.service;

import com.back.domain.product.product.repository.ProductTagMappingRepository;
import com.back.domain.product.tag.dto.request.TagRequest;
import com.back.domain.product.tag.dto.response.TagResponse;
import com.back.domain.product.tag.entity.Tag;
import com.back.domain.product.tag.repository.TagRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    private final TagRepository tagRepository;
    private final ProductTagMappingRepository productTagMappingRepository;

    /** 전체 태그 조회 */
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .toList();
    }

    /** 태그 등록 */
    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByName(request.tagName())){
            log.error("중복 태그로 생성 실패: name={}", request.tagName());
            throw new ServiceException("400", "동일한 이름의 태그가 이미 존재합니다.");
        }

        Tag tag = Tag.builder()
                .name(request.tagName())
                .build();

        tagRepository.save(tag);

        return new TagResponse(tag.getId(), tag.getName());
    }

    /** 태그 수정 */
    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("수정할 테그 없음: id={}", id);
                    return new ServiceException("404", "태그를 찾을 수 없습니다.");
                });

        if (tagRepository.existsByName(request.tagName()) && !tag.getName().equals(request.tagName())){
            log.error("중복 태그로 수정 실패: name={}", request.tagName());
            throw new ServiceException("400", "동일한 이름의 태그가 이미 존재합니다.");
        }

        tag.setName(request.tagName());
        tagRepository.save(tag);

        return new TagResponse(tag.getId(), tag.getName());
    }

    /** 태그 삭제 */
    @Transactional
    public void deleteTag(Long id){
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("삭제할 태그 없음: id={}", id);
                    return new ServiceException("404", "태그를 찾을 수 없습니다.");
                });

        boolean existsMapping = productTagMappingRepository.existsByTag(tag);
        if (existsMapping) {
            log.error("태그에 연결된 상품 존재: id={}", id);
            throw new ServiceException("400", "해당 태그에 연결된 상품이 있어 삭제할 수 없습니다.");
        }

        tagRepository.delete(tag);
        log.info("태그 삭제 완료: id={}", id);
    }
}
