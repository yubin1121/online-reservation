package kr.co.module.api.admin.service;

import kr.co.module.api.admin.dto.CategoryCreateDto;
import kr.co.module.api.admin.dto.CategorySearchDto;
import kr.co.module.api.admin.dto.CategoryUpdateDto;
import kr.co.module.core.domain.Category;
import kr.co.module.core.exception.CategoryNotFoundException;
import kr.co.module.mapper.repository.AdminCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class AdminCategoryService {

    private final MongoTemplate mongoTemplate;
    private final AdminCategoryRepository adminCategoryRepository;
    private final Executor adminQueryExecutor;

    public AdminCategoryService(
            MongoTemplate mongoTemplate,
            AdminCategoryRepository adminCategoryRepository,
            @Qualifier("adminQueryExecutor") Executor adminQueryExecutor
    ) {
        this.mongoTemplate = mongoTemplate;
        this.adminCategoryRepository = adminCategoryRepository;
        this.adminQueryExecutor = adminQueryExecutor;
    }


    // 1. 생성
    public Category createCategory(CategoryCreateDto dto) {
        validateDuplicateCategory(dto.getCategoryName());

        Category category = buildCategory(dto);
        adminCategoryRepository.save(category);

        log.info("Created category: ID={}, Name={}", category.getId(), category.getCategoryName());
        return category;
    }

    // 2. 수정
    public Category updateCategory(CategoryUpdateDto dto) {
        return adminCategoryRepository.findById(dto.getCategoryId())
                .map(category -> {
                    updateCategoryFields(category, dto);
                    return adminCategoryRepository.save(category);
                })
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
    }

    // 3. 삭제
    public Category deleteCategory(CategoryUpdateDto dto) {
        return adminCategoryRepository.findById(dto.getCategoryId())
                .map(category -> {
                    category.setDltYsno("Y");
                    category.setAmnrId(dto.getAdminId());
                    category.setAmndDttm(LocalDateTime.now());
                    return adminCategoryRepository.save(category);
                })
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
    }

    // 4. 검색
    public CompletableFuture<Page<Category>> searchCategories(CategorySearchDto searchDto, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            Query query = buildSearchQuery(searchDto).with(pageable); // 조건 + 페이징
            List<Category> content = mongoTemplate.find(query, Category.class);
            long total = mongoTemplate.count(query, Category.class);
            return new PageImpl<>(content, pageable, total);
        }, adminQueryExecutor);
    }

    private void validateDuplicateCategory(String name) {
        if (adminCategoryRepository.existsByCategoryName(name)) {
            throw new IllegalArgumentException("Duplicate category name: " + name);
        }
    }

    private Category buildCategory(CategoryCreateDto dto) {
        return Category.builder()
                .categoryName(dto.getCategoryName())
                .categoryDesc(dto.getCategoryDesc())
                .categoryOrder(dto.getCategoryOrder())
                .crtrId(dto.getAdminId())
                .amnrId(dto.getAdminId())
                .dltYsno("N")
                .build();
    }

    private void updateCategoryFields(Category category, CategoryUpdateDto dto) {
        if (StringUtils.hasText(dto.getCategoryDesc())) {
            category.setCategoryDesc(dto.getCategoryDesc());
        }
        if (dto.getCategoryOrder() != null) {
            category.setCategoryOrder(dto.getCategoryOrder());
        }
        category.setAmnrId(dto.getAdminId());
        category.setAmndDttm(LocalDateTime.now());
    }

    private Query buildSearchQuery(CategorySearchDto searchDto) {
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(searchDto.getCategoryName())) {
            criteria.and("categoryName").regex(searchDto.getCategoryName(), "i"); // 대소문자 구분 없이 검색
        }
        if (searchDto.getCategoryOrder() != null) {
            criteria.and("categoryOrder").is(searchDto.getCategoryOrder());
        }
        if (StringUtils.hasText(searchDto.getAdminId())) {
            criteria.and("crtrId").is(searchDto.getAdminId());
        }
        criteria.and("dltYsno").ne("Y");

        return new Query(criteria);
    }
}
