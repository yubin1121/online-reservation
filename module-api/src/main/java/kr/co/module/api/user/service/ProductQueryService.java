package kr.co.module.api.user.service;

import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.core.domain.Product;
import kr.co.module.mapper.repository.AdminProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final MongoTemplate mongoTemplate;
    private final AdminProductRepository adminProductRepository;

    @Qualifier("userQueryExecutor")
    private Executor userQueryExecutor;

    // ✅ 비동기 상품 검색
    public CompletableFuture<List<Product>> searchProducts(ProductSearchDto searchDto) {
        return CompletableFuture.supplyAsync(() -> {
            Criteria criteria = new Criteria();
            // 1. 삭제 안 된 상품만
            criteria.and("dltYsno").ne("Y");

            // 2. 옵션 조건들
            if (StringUtils.hasText(searchDto.getProductAdminId())) {
                criteria.and("crtrId").is(searchDto.getProductAdminId());
            }

            if (StringUtils.hasText(searchDto.getCategoryId())) {
                criteria.and("categoryId").is(searchDto.getCategoryId());
            }

            if (StringUtils.hasText(searchDto.getProductName())) {
                criteria.and("productName").regex(searchDto.getProductName(), "i");
            }

            if (StringUtils.hasText(searchDto.getProductPlace())) {
                criteria.and("productPlace").regex(searchDto.getProductPlace(), "i");
            }

            // 날짜 조건
            if (searchDto.getSrchFromDate() != null || searchDto.getSrchToDate() != null) {
                Criteria dateCriteria = new Criteria();
                if (searchDto.getSrchFromDate() != null && searchDto.getSrchToDate() != null) {
                    dateCriteria.andOperator(
                            Criteria.where("productAvlbDateList").gte(searchDto.getSrchFromDate()),
                            Criteria.where("productAvlbDateList").lte(searchDto.getSrchToDate())
                    );
                } else if (searchDto.getSrchFromDate() != null) {
                    dateCriteria.and("productAvlbDateList").gte(searchDto.getSrchFromDate());
                } else {
                    dateCriteria.and("productAvlbDateList").lte(searchDto.getSrchToDate());
                }
                criteria.andOperator(dateCriteria);
            }

            // 시간 조건
            if (StringUtils.hasText(searchDto.getSrchFromTime()) || StringUtils.hasText(searchDto.getSrchToTime())) {
                criteria.and("productAvlbTimeSlots").elemMatch(
                        new Criteria().andOperator(
                                searchDto.getSrchFromTime() != null ?
                                        Criteria.where("value").gte(searchDto.getSrchFromTime()) : new Criteria(),
                                searchDto.getSrchToTime() != null ?
                                        Criteria.where("value").lte(searchDto.getSrchToTime()) : new Criteria()
                        )
                );
            }

            Query query = new Query(criteria);
            List<Product> result = mongoTemplate.find(query, Product.class);
            log.info("상품 검색 결과 수: {}", result.size());
            return result;
        }, userQueryExecutor); // 지정된 Executor에서 실행
    }
}
