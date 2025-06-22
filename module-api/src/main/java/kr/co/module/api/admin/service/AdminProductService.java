package kr.co.module.api.admin.service;

import kr.co.module.api.admin.dto.*;
import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.core.dto.domain.*;
import kr.co.module.core.exception.ProductNotFoundException;
import kr.co.module.mapper.repository.AdminProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final MongoTemplate mongoTemplate;
    private final AdminProductRepository adminProductRepository;

    // 본인(관리자) 상품만 조회
    public List<ProductDto> searchMyProducts(AdminProductSearchDto searchDto) {
        Criteria criteria = Criteria.where("adminId").is(searchDto.getAdminId());
        Query query = new Query(criteria);
        return mongoTemplate.find(query, ProductDto.class);
    }

    private ProductDto buildProduct(ProductCreateDto dto) {
        return ProductDto.builder()
                .categoryId(dto.getCategoryId())
                .productName(dto.getProductName())
                .productDesc(dto.getProductDesc())
                .productPlace(dto.getProductPlace())
                .productLocation(dto.getProductLocation())
                .productAvlbMaxPerSlots(dto.getProductAvlbMaxPerSlots())
                .productAvlbDateList(dto.getProductAvlbDateList())
                .productImgList(dto.getProductImgList())
                .crtrId(dto.getAdminId())
                .amnrId(dto.getAdminId())
                .dltYsno("N")
                .build();
    }

    // 상품 생성
    public ProductDto createProduct(ProductCreateDto productCreateDto) {
        ProductDto product = buildProduct(productCreateDto);
        adminProductRepository.save(product);
        log.info(product.toString());
        return product;
    }



    // 상품 필드 업데이트
    private void updateProductFields(ProductDto product, ProductUpdateDto dto) {
        if (StringUtils.hasText(dto.getProductName())) {
            product.setProductName(dto.getProductName());
        }
        if (StringUtils.hasText(dto.getCategoryId())) {
            product.setCategoryId(dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getProductDesc())) {
            product.setProductDesc(dto.getProductDesc());
        }
        if (StringUtils.hasText(dto.getProductPlace())) {
            product.setProductPlace(dto.getProductPlace());
        }
        if (StringUtils.hasText(dto.getProductLocation())) {
            product.setProductLocation(dto.getProductLocation());
        }
        if (!CollectionUtils.isEmpty(dto.getProductImgList())) {
            product.setProductImgList(dto.getProductImgList());
        }
        if (!CollectionUtils.isEmpty(dto.getProductAvlbDateList())) {
            product.setProductAvlbDateList(dto.getProductAvlbDateList());
        }
        if (!CollectionUtils.isEmpty(dto.getProductAvlbTimeSlots())) {
            product.setProductAvlbTimeSlots(dto.getProductAvlbTimeSlots());
        }
        if (!CollectionUtils.isEmpty(dto.getProductAvlbMaxPerSlots())) {
            product.setProductAvlbMaxPerSlots(dto.getProductAvlbMaxPerSlots());
        }
        if (dto.getTotalQuantity() != null) {
            product.setTotalQuantity(dto.getTotalQuantity());
        }
        product.setAmndDttm(LocalDateTime.now());
    }

    // 상품 수정
    public ProductDto updateProduct(ProductUpdateDto dto) {
        return adminProductRepository.findById(dto.getProductId())
                .filter(product -> product.getCrtrId().equals(dto.getAdminId()))
                .filter(product -> product.getAmnrId() != null && product.getAmnrId().equals(dto.getAdminId()))
                .map(product -> {
                    updateProductFields(product, dto);
                    product.setAmnrId(dto.getAdminId());
                    return adminProductRepository.save(product);
                })
                .orElseThrow(() -> new ProductNotFoundException(dto.getProductId()));
    }

    // 상품 삭제
    public ProductDto deleteProduct(ProductUpdateDto dto) {
        return adminProductRepository.findById(dto.getProductId())
                .filter(product -> product.getCrtrId().equals(dto.getAdminId()))  // 생성자 확인
                .filter(product -> product.getAmnrId() != null && product.getAmnrId().equals(dto.getAdminId()))  // 마지막 수정자 확인
                .map(product -> {
                    product.setDltYsno("Y");
                    product.setAmnrId(dto.getAdminId());
                    product.setAmndDttm(LocalDateTime.now());
                    return adminProductRepository.save(product);
                })
                .orElseThrow(() -> new ProductNotFoundException(dto.getProductId()));
    }

    // 상품 검색
    public Page<ProductDto> searchProducts(ProductSearchDto searchDto, Pageable pageable) {
        Query query = buildSearchQuery(searchDto).with(pageable);
        List<ProductDto> content = mongoTemplate.find(query, ProductDto.class);
        long total = mongoTemplate.count(query, ProductDto.class);

        return new PageImpl<>(content, pageable, total);
    }


    // 검색 쿼리 빌더
    private Query buildSearchQuery(ProductSearchDto searchDto) {
        Criteria criteria = new Criteria();
        // 상품 관리자 아이디
        if (StringUtils.hasText(searchDto.getProductAdminId())) {
            criteria.and("productAdminId").is(searchDto.getProductAdminId());
        }
        // 카테고리 아이디
        if (StringUtils.hasText(searchDto.getCategoryId())) {
            criteria.and("categoryId").is(searchDto.getCategoryId());
        }
        // 상품명
        if (StringUtils.hasText(searchDto.getProductName())) {
            criteria.and("productName").regex(searchDto.getProductName(), "i");
        }
        // 상품 장소
        if (StringUtils.hasText(searchDto.getProductPlace())) {
            criteria.and("productPlace").regex(searchDto.getProductPlace(), "i");
        }
        // 날짜 범위
        if (StringUtils.hasText(searchDto.getSrchFromDate())) {
            criteria.and("productAvlbDateList").gte(searchDto.getSrchFromDate());
        }
        if (StringUtils.hasText(searchDto.getSrchToDate())) {
            criteria.and("productAvlbDateList").lte(searchDto.getSrchToDate());
        }

        // 시간 범위
        if (StringUtils.hasText(searchDto.getSrchFromTime())) {
            criteria.and("productAvlbTimeSlots").gte(searchDto.getSrchFromTime());
        }
        if (StringUtils.hasText(searchDto.getSrchToTime())) {
            criteria.and("productAvlbTimeSlots").lte(searchDto.getSrchToTime());
        }

        criteria.and("dltYsno").ne("Y");


        return new Query(criteria);
    }

}