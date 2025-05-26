package kr.co.module.api.admin.command.service;


import kr.co.module.api.admin.command.dto.CategoryCreateDto;
import kr.co.module.api.admin.command.dto.CategoryUpdateDto;
import kr.co.module.core.dto.domain.CategoryDto;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CategoryCommandService {

    @Getter
    private final List<CategoryDto> categoryList = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // 카테고리 생성
    public boolean createCategory(CategoryCreateDto categoryCreateDto) {
        CategoryDto newCategory = new CategoryDto();
        newCategory.setCategoryId(idGenerator.getAndIncrement());
        newCategory.setCategoryName(categoryCreateDto.getCategoryName());
        newCategory.setCategoryDesc(categoryCreateDto.getCategoryDesc());
        newCategory.setCategoryOrder(categoryCreateDto.getCategoryOrder());
        newCategory.setCrtrId(categoryCreateDto.getAdminId());
        newCategory.setCretDttm(LocalDateTime.now());
        newCategory.setAmnrId(categoryCreateDto.getAdminId());
        newCategory.setAmndDttm(LocalDateTime.now());
        newCategory.setDltYsno("N");
        categoryList.add(newCategory);

        System.out.println(newCategory);
        return true;
    }

    // 카테고리 수정
    public boolean updateCategory(CategoryUpdateDto categoryUpdateDto) {
        Optional<CategoryDto> categoryOpt = categoryList.stream()
                .filter(c -> c.getCategoryId().equals(categoryUpdateDto.getCategoryId()))
                .findFirst();
        if (categoryOpt.isPresent()) {
            // 카테고리 설명 수정
            if (categoryUpdateDto.getCategoryDesc() != null && !categoryUpdateDto.getCategoryDesc().isBlank()) {
                categoryOpt.get().setCategoryDesc(categoryUpdateDto.getCategoryDesc());
            }
            // 정렬순서 수정
            if (categoryUpdateDto.getCategoryOrder() != null) {
                categoryOpt.get().setCategoryOrder(categoryUpdateDto.getCategoryOrder());
            }
            // 수정자, 수정일시 갱신
            categoryOpt.get().setAmnrId(categoryUpdateDto.getAdminId());
            categoryOpt.get().setAmndDttm(LocalDateTime.now());
            return true;
        }
        return false;
    }

    // 카테고리 삭제
    public boolean deleteCategory(CategoryUpdateDto categoryUpdateDto) {
        Optional<CategoryDto> categoryOpt = categoryList.stream()
                .filter(c -> c.getCategoryId().equals(categoryUpdateDto.getCategoryId()))
                .findFirst();
        if (categoryOpt.isPresent()) {
            categoryOpt.get().setDltYsno("Y");
            categoryOpt.get().setAmnrId(categoryUpdateDto.getAdminId());
            categoryOpt.get().setAmndDttm(LocalDateTime.now());
            return true;
        }
        return false;
    }

}
