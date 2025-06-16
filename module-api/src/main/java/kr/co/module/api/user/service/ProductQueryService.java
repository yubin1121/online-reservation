package kr.co.module.api.user.service;

import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.mapper.repository.AdminProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final MongoTemplate mongoTemplate;
    private final AdminProductRepository adminProductRepository;

    public List<ProductDto> searchProducts(ProductSearchDto searchDto) {
        Criteria criteria = new Criteria();

        // 1. 기본 조건: 삭제되지 않은 상품
        criteria.and("dltYsno").ne("Y");

        // 2. 관리자 ID (특정 판매자 상품 검색)
        if (StringUtils.hasText(searchDto.getProductAdminId())) {
            criteria.and("crtrId").is(searchDto.getProductAdminId());
        }

        // 3. 카테고리 ID
        if (StringUtils.hasText(searchDto.getCategoryId())) {
            criteria.and("categoryId").is(searchDto.getCategoryId());
        }

        // 4. 상품명
        if (StringUtils.hasText(searchDto.getProductName())) {
            criteria.and("productName").regex(searchDto.getProductName(), "i");
        }

        // 5. 장소
        if (StringUtils.hasText(searchDto.getProductPlace())) {
            criteria.and("productPlace").regex(searchDto.getProductPlace(), "i");
        }

        // 6. 날짜 범위
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

        // 7. 시간 범위
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
        return mongoTemplate.find(query, ProductDto.class);
    }
}
