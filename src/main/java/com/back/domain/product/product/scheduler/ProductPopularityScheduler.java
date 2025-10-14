package com.back.domain.product.product.scheduler;

import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPopularityScheduler {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistRepository wishlistRepository;

    // 매일 한국 시간 기준 오전 10시 30분에 실행 (이후 실제 서비스 운영한다면 매일 00:20시에 실행되도록 수정)
    @Scheduled(cron = "0 30 10 * * *", zone = "Asia/Seoul")
    @Transactional
    public void updatePopularityScores() {
        log.info("상품 인기 점수 계산 스케줄러 시작");
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            // 판매 수 계산 (가중치 60%)
            long salesCount = orderItemRepository.countByProduct(product);

            // 찜 수 계산 (가중치 20%)
            long wishlistCount = wishlistRepository.countByProduct(product);

            // 리뷰 평점 (가중치 20%)
            Double averageRating = product.getAverageRating();
            if (averageRating == null) {
                averageRating = 0.0;
            }

            // 최종 점수 계산
            int popularityScore = (int) (salesCount * 0.6 + wishlistCount * 0.2 + averageRating * 0.2);

            // 상품에 점수 업데이트
            product.updatePopularityScore(popularityScore);
        }
        log.info("{}개 상품에 대한 인기 점수 업데이트 완료.", products.size());
    }
}