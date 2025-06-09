package kr.co.module.api.user.service;

import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.mapper.repository.AdminProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ProductQueryService.class);

    private final MongoTemplate mongoTemplate;
    private final AdminProductRepository adminProductRepository;

    public ProductQueryService(MongoTemplate mongoTemplate, AdminProductRepository adminProductRepository ) {
        this.mongoTemplate = mongoTemplate;
        this.adminProductRepository = adminProductRepository;
    }

    public List<ProductDto> searchProducts(ProductSearchDto searchDto) {
        Criteria criteria = new Criteria();

        // 삭제여부(Y 제외)
        criteria.and("dltYsno").ne("Y");

        // 관리자 ID
        if (searchDto.getProductAdminId() != null ) {
            criteria.and("crtrId").is(searchDto.getProductAdminId());
        }

        // 카테고리 ID
        if (searchDto.getCategoryId() != null ) {
            criteria.and("categoryId").is(searchDto.getCategoryId());
        }

        // 상품명
        if (searchDto.getProductName() != null && !searchDto.getProductName().isBlank()) {
            criteria.and("productName").regex(".*" + searchDto.getProductName() + ".*");
        }

        // 장소
        if (searchDto.getProductPlace() != null && !searchDto.getProductPlace().isBlank()) {
            criteria.and("productPlace").regex(".*" + searchDto.getProductPlace() + ".*");
        }

        // 날짜 범위 (productAvlbDateList에 하나라도 포함되면 통과)
        if (searchDto.getSrchFromDate() != null || searchDto.getSrchToDate() != null) {
            Criteria dateCriteria = Criteria.where("productAvlbDateList");
            if (searchDto.getSrchFromDate() != null && searchDto.getSrchToDate() != null) {
                dateCriteria.elemMatch(Criteria.where("$gte").is(searchDto.getSrchFromDate()).andOperator(Criteria.where("$lte").is(searchDto.getSrchToDate())));
            } else if (searchDto.getSrchFromDate() != null) {
                dateCriteria.elemMatch(Criteria.where("$gte").is(searchDto.getSrchFromDate()));
            } else {
                dateCriteria.elemMatch(Criteria.where("$lte").is(searchDto.getSrchToDate()));
            }
            criteria.andOperator(dateCriteria);
        }

        // 시간 범위
        Query query = new Query(criteria);
        List<ProductDto> result = mongoTemplate.find(query, ProductDto.class);

        // 시간 범위 추가 필터링 (productAvlbTimeSlots가 Map<String, List<String>> 구조라고 가정)
        if (searchDto.getSrchFromTime() != null || searchDto.getSrchToTime() != null) {
            result = result.stream().filter(p -> {
                if (p.getProductAvlbTimeSlots() == null) return false;
                return p.getProductAvlbTimeSlots().values().stream().flatMap(List::stream).anyMatch(time ->
                        (searchDto.getSrchFromTime() == null || time.compareTo(searchDto.getSrchFromTime()) >= 0) &&
                                (searchDto.getSrchToTime() == null || time.compareTo(searchDto.getSrchToTime()) <= 0)
                );
            }).toList();
        }

        return result;
    }
}
