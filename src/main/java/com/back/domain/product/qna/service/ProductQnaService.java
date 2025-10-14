package com.back.domain.product.qna.service;

import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.service.ProductService;
import com.back.domain.product.qna.dto.request.ProductQnaRequestDto;
import com.back.domain.product.qna.dto.response.ProductQnaListResponseDto;
import com.back.domain.product.qna.dto.response.ProductQnaResponseDto;
import com.back.domain.product.qna.entity.ProductQna;
import com.back.domain.product.qna.entity.ProductQnaImage;
import com.back.domain.product.qna.repository.ProductQnaImageRepository;
import com.back.domain.product.qna.repository.ProductQnaRepository;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.s3.S3FileRequest;
import com.back.global.s3.S3ValidationService;
import com.back.global.s3.UploadResultResponse;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQnaService {

    private final ProductQnaRepository productQnaRepository;
    private final ProductQnaImageRepository productQnaImageRepository;
    private final S3ValidationService s3ValidationService;
    private final ProductService productService;

    /** 상품 Q&A 등록 */
    @Transactional
    public UUID createProductQna(UUID productUuid, ProductQnaRequestDto request, CustomUserDetails customUserDetails) {
        Product product = productService.getProductOrThrow(productUuid);
        User user = customUserDetails.getUser();

        ProductQna productQna = ProductQna.builder()
                .product(product)
                .user(user)
                .qnaCategory(request.qnaCategory())
                .qnaTitle(request.qnaTitle())
                .qnaDescription(request.qnaDescription())
                .build();

        if (request.qnaImages() != null && !request.qnaImages().isEmpty()) {
            // productQnaImage DB에 저장
            productQna.getProductQnaImages().addAll(buildProductQnaImages(productQna, request.qnaImages()));
        }

        productQnaRepository.save(productQna);
        return product.getProductUuid();
    }

    /** 상품 Q&A 상세 조회 */
    @Transactional(readOnly = true)
    public ProductQnaResponseDto getProductQnaDetail(Long productQnaId) {
        ProductQna productQna = productQnaRepository.findById(productQnaId)
                .orElseThrow(() -> new ServiceException("404", "해당 상품 Q&A를 찾을 수 없습니다."));

        // ProductQna의 이미지 리스트
        List<UploadResultResponse> qnaImages = productQna.getProductQnaImages().stream()
                .map(image -> new UploadResultResponse(image.getFileUrl(), image.getFileType(), image.getS3Key(), image.getOriginalFileName()))
                .toList();

        // 날짜 포맷 "YY.MM.DD" 형식으로 변경
        String formattedCreateDate = productQna.getCreateDate().format(DateTimeFormatter.ofPattern("yy.MM.dd"));

        // ProductQna 엔티티 -> DTO 변환
        return new ProductQnaResponseDto(
                productQna.getId(),
                productQna.getQnaCategory(),
                productQna.getQnaTitle(),
                productQna.getQnaDescription(),
                productQna.getUser().getName(),
                formattedCreateDate,
                qnaImages
        );
    }

    /** 상품 Q&A 목록 조회 (페이지네이션) */
    @Transactional(readOnly = true)
    public ProductQnaListResponseDto getProductQnaList(UUID productUuid, int page, int size) {
        Product product = productService.getProductOrThrow(productUuid);

        // 페이지네이션
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createDate"));
        // 페이지네이션 정보에 따라 ProductQna 조회
        Page<ProductQna> qnaPage = productQnaRepository.findByProduct(product, pageable);

        // 조회된 ProductQna -> DTO 변환
        List<ProductQnaResponseDto> qnaList = qnaPage.getContent().stream()
                .map(productQna -> {
                    // 이미지
                    List<UploadResultResponse> qnaImages = productQna.getProductQnaImages().stream()
                            .map(image -> new UploadResultResponse(image.getFileUrl(), image.getFileType(), image.getS3Key(), image.getOriginalFileName()))
                            .toList();
                    // 날짜 포맷팅
                    String formattedCreateDate = productQna.getCreateDate().format(DateTimeFormatter.ofPattern("yy.MM.dd"));

                    return new ProductQnaResponseDto(
                            productQna.getId(),
                            productQna.getQnaCategory(),
                            productQna.getQnaTitle(),
                            productQna.getQnaDescription(),
                            productQna.getUser().getName(),
                            formattedCreateDate,
                            qnaImages
                    );
                })
                .toList();

        return ProductQnaListResponseDto.fromPage(qnaPage, qnaList);
    }

    /** 상품 Q&A 이미지 저장 */
    private List<ProductQnaImage> buildProductQnaImages(ProductQna productQna, List<S3FileRequest> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .map(img -> {
                    s3ValidationService.validateFileExists(img.s3Key());
                    return ProductQnaImage.builder()
                            .productQna(productQna)
                            .fileUrl(img.url())
                            .fileType(img.type())
                            .s3Key(img.s3Key())
                            .originalFileName(img.originalFileName())
                            .build();
                }).toList();
    }
}
