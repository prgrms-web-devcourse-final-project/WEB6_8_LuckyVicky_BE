package com.back.domain.funding.repository;


import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FundingCustomRepositoryImpl implements FundingCustomRepository {

    private final EntityManager em;

    @Override
    public Page<Funding> findByFilters(
            Set<FundingStatus> statuses,
            String keyword,
            Long categoryId,
            Long minPrice,
            Long maxPrice,
            Pageable pageable
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 데이터 조회 쿼리
        CriteriaQuery<Funding> query = cb.createQuery(Funding.class);
        Root<Funding> funding = query.from(Funding.class);

        // LEFT JOIN FETCH로 N+1 방지
        funding.fetch("user", JoinType.LEFT);

        // WHERE 조건 생성
        List<Predicate> predicates = buildPredicates(cb, funding, statuses, keyword, categoryId, minPrice, maxPrice);
        query.where(predicates.toArray(new Predicate[0]));

        // 정렬 적용
        query.orderBy(buildOrders(cb, funding, pageable.getSort()));

        // DISTINCT 적용 (join 시 중복 제거)
        query.distinct(true);

        // 페이징 적용
        TypedQuery<Funding> typedQuery = em.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Funding> results = typedQuery.getResultList();

        // 카운트 쿼리 (페이징을 위한 전체 개수)
        long total = countByFilters(cb, statuses, keyword, categoryId, minPrice, maxPrice);

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * WHERE 조건 생성
     */
    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Funding> funding,
            Set<FundingStatus> statuses,
            String keyword,
            Long categoryId,
            Long minPrice,
            Long maxPrice
    ) {
        List<Predicate> predicates = new ArrayList<>();

        // 상태 필터
        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(funding.get("status").in(statuses));
        }

        // 제목 검색 (대소문자 무시)
        if (keyword != null && !keyword.isBlank()) {
            predicates.add(cb.like(
                    cb.lower(funding.get("title")),
                    "%" + keyword.toLowerCase() + "%"
            ));
        }

        if (categoryId != null) {
            predicates.add(cb.equal(funding.get("category").get("id"), categoryId));
        }

        // 가격 범위 필터 (Funding 엔티티의 price 필드 사용)
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(funding.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(funding.get("price"), maxPrice));
        }

        return predicates;
    }

    /**
     * 정렬 조건 생성
     */
    private List<Order> buildOrders(CriteriaBuilder cb, Root<Funding> funding, Sort sort) {
        List<Order> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Path<Object> path = funding.get(order.getProperty());
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        }

        // 기본 정렬: 최신순
        if (orders.isEmpty()) {
            orders.add(cb.desc(funding.get("createDate")));
        }

        return orders;
    }

    /**
     * 전체 개수 조회 (카운트 쿼리)
     */
    private long countByFilters(
            CriteriaBuilder cb,
            Set<FundingStatus> statuses,
            String keyword,
            Long categoryId,
            Long minPrice,
            Long maxPrice
    ) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Funding> funding = countQuery.from(Funding.class);

        // WHERE 조건 동일하게 적용
        List<Predicate> predicates = buildPredicates(cb, funding, statuses, keyword, categoryId, minPrice, maxPrice);
        countQuery.select(cb.count(funding));
        countQuery.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(countQuery).getSingleResult();
    }
}