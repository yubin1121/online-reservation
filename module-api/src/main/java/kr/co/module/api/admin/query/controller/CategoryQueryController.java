package kr.co.module.api.admin.query.controller;
import kr.co.module.api.admin.query.dto.CategorySearchDto;
import kr.co.module.api.admin.query.service.CategoryQueryService;

import kr.co.module.core.dto.domain.CategoryDto;
import kr.co.module.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category/query")
public class CategoryQueryController {

    private final CategoryQueryService categoryQueryService;

    public CategoryQueryController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> searchCategories(
            @ModelAttribute CategorySearchDto searchDto
    ) {
        List<CategoryDto> result = categoryQueryService.searchCategories(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "카테고리 조회 성공", null));
    }


}
