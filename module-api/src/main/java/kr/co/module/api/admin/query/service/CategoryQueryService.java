package kr.co.module.api.admin.query.service;
import java.util.List;
import java.util.stream.Collectors;

import kr.co.module.api.admin.query.dto.CategorySearchDto;
import kr.co.module.core.dto.domain.CategoryDto;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueryService {
    private final List<CategoryDto> categoryList;

    public CategoryQueryService(List<CategoryDto> categoryList) {
        this.categoryList = categoryList;
    }

    public List<CategoryDto> searchCategories(CategorySearchDto searchDto) {
        return categoryList.stream()
                .filter(c -> searchDto.getCategoryName() == null || c.getCategoryName().contains(searchDto.getCategoryName()))
                .filter(c -> searchDto.getCategoryOrder() == null || c.getCategoryOrder() != null && c.getCategoryOrder().equals(searchDto.getCategoryOrder()))
                .filter(c -> searchDto.getAdminId() == null || c.getCrtrId().equals(searchDto.getAdminId()))
                .collect(Collectors.toList());
    }
}
