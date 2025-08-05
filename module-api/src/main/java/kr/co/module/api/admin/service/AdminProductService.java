package kr.co.module.api.admin.service;

import kr.co.module.api.admin.dto.*;
import kr.co.module.api.common.service.ImageUploadService;
import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.core.domain.Product;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final MongoTemplate mongoTemplate;
    private final AdminProductRepository adminProductRepository;
    private final ImageUploadService imageUploadService;


    // 본인(관리자) 상품만 조회
    public List<Product> searchMyProducts(AdminProductSearchDto searchDto) {
        Criteria criteria = Criteria.where("adminId").is(searchDto.getAdminId());
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Product.class);
    }

    private Product buildProduct(ProductCreateDto dto) {
        return Product.builder()
                .categoryId(dto.getCategoryId())
                .productName(dto.getProductName())
                .productDesc(dto.getProductDesc())
                .productPlace(dto.getProductPlace())
                .productLocation(dto.getProductLocation())
                .productAvlbMaxPerSlots(dto.getProductAvlbMaxPerSlots())
                .productAvlbDateList(dto.getProductAvlbDateList())
                //.productImgList(dto.getProductImgList())
                .crtrId(dto.getAdminId())
                .amnrId(dto.getAdminId())
                .dltYsno("N")
                .build();
    }

    // 상품 생성
    public CompletableFuture<Optional<Product>> createProduct(ProductCreateDto productCreateDto) {
        Product product = buildProduct(productCreateDto);
        Product savedProduct = adminProductRepository.save(product);

        // 이미지 업로드도 비동기, 이미지까지 저장된 Product를 담은 Optional를 비동기로 리턴
        return imageUploadService.uploadProductImagesAsync(productCreateDto.getProductImages(), savedProduct.getId())
                .thenApplyAsync(imageUrls -> {
                    savedProduct.setProductImgList(imageUrls);
                    Product updated = adminProductRepository.save(savedProduct);
                    log.info("Product + images saved: {}", updated.getId());
                    return Optional.of(updated);
                })
                .exceptionally(ex -> {
                    log.error("이미지 업로드 실패: {}", ex.getMessage());
                    return Optional.empty();
                });
    }



    // 상품 필드 업데이트
    private void updateProductFields(Product product, ProductUpdateDto dto) {
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
        // case 1: 새로운 이미지가 있을 경우
        if (!CollectionUtils.isEmpty(dto.getNewProductImages())) {
            List<String> oldImageUrls = product.getProductImgList();
            if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
                // 기존 이미지 비동기 삭제
                imageUploadService.deleteProductImagesAsync(oldImageUrls)
                        .exceptionally(ex -> {
                            log.error("Failed to asynchronously delete old images for product {}: {}", product.getId(), ex.getMessage());
                            return null;
                        });
            }
            // 새 이미지 비동기 업로드
            imageUploadService.uploadProductImagesAsync(dto.getNewProductImages(), product.getId())
                    .thenAccept(newImageUrls -> {
                        // 새로운 이미지 URL을 Product 엔티티에 설정
                        product.setProductImgList(newImageUrls);
                        // 업데이트된 Product 저장
                        adminProductRepository.save(product);
                        log.info("Product image URLs updated asynchronously for product: {}", product.getId());
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to asynchronously upload new images and update product for product {}: {}", product.getId(), ex.getMessage());
                        // 이미지 업로드 실패 시 대체 로직 또는 롤백 고려
                        return null;
                    });
        }
        // case 2: productImgList (List<String>)가 DTO에 명시적으로 넘어왔는데, newProductImages는 없을 경우
        else if (dto.getProductImgList() != null) { // newProductImages가 없고 productImgList가 있을 경우
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
    public CompletableFuture<Optional<Product>> updateProduct(ProductUpdateDto dto) {
        Optional<Product> opt = adminProductRepository.findById(dto.getProductId())
                .filter(product -> product.getCrtrId().equals(dto.getAdminId()))
                .filter(product -> product.getAmnrId() != null && product.getAmnrId().equals(dto.getAdminId()));

        if (opt.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());

        Product product = opt.get();
        updateProductFields(product, dto);
        product.setAmnrId(dto.getAdminId());

        // 만약 새 이미지가 있을 때만 비동기 업로드가 의미 있으므로
        if (!CollectionUtils.isEmpty(dto.getNewProductImages())) {
            return imageUploadService.uploadProductImagesAsync(dto.getNewProductImages(), product.getId())
                    .thenApplyAsync(newImageUrls -> {
                        product.setProductImgList(newImageUrls);
                        Product saved = adminProductRepository.save(product);
                        return Optional.of(saved);
                    })
                    .exceptionally(ex -> {
                        log.error("이미지 업로드 실패(수정): {}", ex.getMessage());
                        return Optional.empty();
                    });
        } else {
            // 이미지 관련 변경이 없다면 그냥 동기저장
            Product saved = adminProductRepository.save(product);
            return CompletableFuture.completedFuture(Optional.of(saved));
        }
    }


    // 상품 삭제
    public CompletableFuture<Optional<Product>> deleteProduct(ProductUpdateDto dto) {
        return CompletableFuture.supplyAsync(() ->
                adminProductRepository.findById(dto.getProductId())
                        .filter(product -> product.getCrtrId().equals(dto.getAdminId()))
                        .filter(product -> product.getAmnrId() != null && product.getAmnrId().equals(dto.getAdminId()))
                        .map(product -> {
                            product.setDltYsno("Y");
                            product.setAmnrId(dto.getAdminId());
                            product.setAmndDttm(LocalDateTime.now());
                            return adminProductRepository.save(product);
                        })
        );
    }


    // 상품 검색
    public Page<Product> searchProducts(ProductSearchDto searchDto, Pageable pageable) {
        Query query = buildSearchQuery(searchDto).with(pageable);
        List<Product> content = mongoTemplate.find(query, Product.class);
        long total = mongoTemplate.count(query, Product.class);

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