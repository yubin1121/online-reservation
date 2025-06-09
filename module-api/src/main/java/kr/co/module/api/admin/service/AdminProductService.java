package kr.co.module.api.admin.service;

import kr.co.module.api.admin.dto.*;
import kr.co.module.core.dto.domain.*;
import kr.co.module.mapper.repository.AdminProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminProductService {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductService.class);

    private final MongoTemplate mongoTemplate;

    private final AdminProductRepository adminProductRepository;

    public AdminProductService(MongoTemplate mongoTemplate, AdminProductRepository adminProductRepository) {
        this.mongoTemplate = mongoTemplate;
        this.adminProductRepository = adminProductRepository;
    }


    // 본인(관리자) 상품만 조회
    public List<ProductDto> searchMyProducts(AdminProductSearchDto searchDto) {
        Criteria criteria = Criteria.where("adminId").is(searchDto.getAdminId());
        Query query = new Query(criteria);
        return mongoTemplate.find(query, ProductDto.class);
    }

    // 상품 생성
    public ProductDto createProduct(ProductCreateDto productCreateDto) {
        ProductDto product = ProductDto.builder()
                .categoryId(productCreateDto.getCategoryId())
                .productName(productCreateDto.getProductName())
                .productDesc(productCreateDto.getProductDesc())
                .productPlace(productCreateDto.getProductPlace())
                .productLocation(productCreateDto.getProductLocation())
                .productImgList(productCreateDto.getProductImgList())
                .productAvlbDateList(productCreateDto.getProductAvlbDateList())
                .productAvlbTimeSlots(productCreateDto.getProductAvlbTimeSlots())
                .productAvlbMaxPerSlots(productCreateDto.getProductAvlbMaxPerSlots())
                .totalQuantity(productCreateDto.getTotalQuantity())
                .crtrId(productCreateDto.getAdminId())
                .cretDttm(LocalDateTime.now())
                .amnrId(productCreateDto.getAdminId())
                .amndDttm(LocalDateTime.now())
                .dltYsno("N")
                .build();


        adminProductRepository.save(product);
        logger.info(product.toString());
        return product;
    }

    // 상품 수정
    public ProductDto updateProduct(ProductUpdateDto productUpdateDto) {
        return adminProductRepository.findById(productUpdateDto.getProductId())
                .filter(product -> product.getCrtrId().equals(productUpdateDto.getAdminId())) // 본인 상품만
                .map(product -> {
                    if (productUpdateDto.getCategoryId() != null) product.setCategoryId(productUpdateDto.getCategoryId());
                    if (productUpdateDto.getProductName() != null && !productUpdateDto.getProductName().isBlank()) product.setProductName(productUpdateDto.getProductName());
                    if (productUpdateDto.getProductDesc() != null && !productUpdateDto.getProductDesc().isBlank()) product.setProductDesc(productUpdateDto.getProductDesc());
                    if (productUpdateDto.getProductPlace() != null && !productUpdateDto.getProductPlace().isBlank()) product.setProductPlace(productUpdateDto.getProductPlace());
                    if (productUpdateDto.getProductLocation() != null && !productUpdateDto.getProductLocation().isBlank()) product.setProductLocation(productUpdateDto.getProductLocation());
                    if (productUpdateDto.getProductImgList() != null) product.setProductImgList(productUpdateDto.getProductImgList());
                    if (productUpdateDto.getProductAvlbDateList() != null) product.setProductAvlbDateList(productUpdateDto.getProductAvlbDateList());
                    if (productUpdateDto.getProductAvlbTimeSlots() != null) product.setProductAvlbTimeSlots(productUpdateDto.getProductAvlbTimeSlots());
                    if (productUpdateDto.getProductAvlbMaxPerSlots() != null) product.setProductAvlbMaxPerSlots(productUpdateDto.getProductAvlbMaxPerSlots());
                    if (productUpdateDto.getTotalQuantity() != null) product.setTotalQuantity(productUpdateDto.getTotalQuantity());
                    product.setAmndDttm(LocalDateTime.now());
                    logger.info(product.toString());
                    return adminProductRepository.save(product);
                })
                .orElse(null);
    }


    // 상품 삭제
    public ProductDto deleteProduct(ProductUpdateDto productUpdateDto) {
        return adminProductRepository.findById(productUpdateDto.getProductId())
                .filter(product -> product.getCrtrId().equals(productUpdateDto.getAdminId()))
                .map(product -> {
                    product.setDltYsno("Y");
                    product.setAmndDttm(LocalDateTime.now());
                    logger.info(product.toString());
                    return adminProductRepository.save(product);
                })
                .orElse(null);
    }

}