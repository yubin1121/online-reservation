package kr.co.module.api.admin.service;


import kr.co.module.api.admin.dto.CategoryCreateDto;
import kr.co.module.api.admin.dto.CategorySearchDto;
import kr.co.module.api.admin.dto.CategoryUpdateDto;
import kr.co.module.core.dto.domain.CategoryDto;
import kr.co.module.mapper.repository.AdminCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryService.class);

    private final MongoTemplate mongoTemplate;

    private final AdminCategoryRepository adminCategoryRepository;

    public AdminCategoryService(MongoTemplate mongoTemplate, AdminCategoryRepository adminCategoryRepository) {
        this.mongoTemplate = mongoTemplate;
        this.adminCategoryRepository = adminCategoryRepository;
    }

    // 카테고리 생성
    public CategoryDto createCategory(CategoryCreateDto categoryCreateDto) {
        CategoryDto category = CategoryDto.builder()
                .categoryName(categoryCreateDto.getCategoryName())
                .categoryDesc(categoryCreateDto.getCategoryDesc())
                .categoryOrder(categoryCreateDto.getCategoryOrder())
                .crtrId(categoryCreateDto.getAdminId())
                .cretDttm(LocalDateTime.now())
                .amnrId(categoryCreateDto.getAdminId())
                .amndDttm(LocalDateTime.now())
                .dltYsno("N")
                .build();

        adminCategoryRepository.save(category);
        logger.info(category.toString());
        return category;
    }

    // 카테고리 수정
    public CategoryDto updateCategory(CategoryUpdateDto categoryUpdateDto) {
        return adminCategoryRepository.findById(categoryUpdateDto.getCategoryId())
                .map(category -> {
                    if (categoryUpdateDto.getCategoryDesc() != null && !categoryUpdateDto.getCategoryDesc().isBlank()) {
                        category.setCategoryDesc(categoryUpdateDto.getCategoryDesc());
                    }
                    if (categoryUpdateDto.getCategoryOrder() != null) {
                        category.setCategoryOrder(categoryUpdateDto.getCategoryOrder());
                    }
                    category.setAmnrId(categoryUpdateDto.getAdminId());
                    category.setAmndDttm(LocalDateTime.now());
                    adminCategoryRepository.save(category);
                    return category;
                })
                .orElse(null);
    }


    // 카테고리 삭제
    public CategoryDto deleteCategory(CategoryUpdateDto categoryUpdateDto) {

        return adminCategoryRepository.findById(categoryUpdateDto.getCategoryId())
                .map(category -> {
                    category.setDltYsno("Y");
                    category.setAmnrId(categoryUpdateDto.getAdminId());
                    category.setAmndDttm(LocalDateTime.now());
                    adminCategoryRepository.save(category);
                    return category;
                })
                .orElse(null);
    }

    public List<CategoryDto> searchCategories(CategorySearchDto searchDto) {

        Criteria criteria = new Criteria();

        // 동적 조건 추가
        if (searchDto.getCategoryName() != null && !searchDto.getCategoryName().isBlank()) {
            criteria.and("categoryName").regex(".*" + searchDto.getCategoryName() + ".*"); // 부분 일치
        }
        if (searchDto.getCategoryOrder() != null) {
            criteria.and("categoryOrder").is(searchDto.getCategoryOrder());
        }
        if (searchDto.getAdminId() != null && !searchDto.getAdminId().isBlank()) {
            criteria.and("crtrId").is(searchDto.getAdminId());
        }
        // 삭제된 건 제외
        criteria.and("dltYsno").ne("Y");

        Query query = new Query(criteria);
        return mongoTemplate.find(query, CategoryDto.class);
    }

}
