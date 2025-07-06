package kr.co.module.api.admin.controller;
import jakarta.validation.Valid;
import kr.co.module.api.admin.dto.*;

import kr.co.module.api.admin.service.AdminCategoryService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.domain.Category;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category/")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    // 카테고리 등록
    @PostMapping("register")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        Category result = adminCategoryService.createCategory(categoryCreateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "카테고리 등록 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.CATEGORY_CREATE_FAIL.message(), ErrorCode.CATEGORY_CREATE_FAIL.code(), null)
            );
        }
    }

    // 카테고리 수정
    @PutMapping("update/{categoryId}")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoryUpdateDto categoryUpdateDto) {
        Category result = adminCategoryService.updateCategory(categoryUpdateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "카테고리 수정 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.CATEGORY_NOT_FOUND.message(), ErrorCode.CATEGORY_NOT_FOUND.code(), null)
            );
        }
    }

    // 카테고리 삭제
    @DeleteMapping("delete/{categoryId}")
    public ResponseEntity<?> deleteCategory(@Valid @RequestBody CategoryUpdateDto categoryUpdateDto) {
        Category result = adminCategoryService.deleteCategory(categoryUpdateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "카테고리 삭제 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.CATEGORY_NOT_FOUND.message(), ErrorCode.CATEGORY_NOT_FOUND.code(), null)
            );
        }
    }


    @GetMapping("search")
    public ResponseEntity<Page<Category>> searchCategories(
            @ModelAttribute CategorySearchDto searchDto,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Category> result = adminCategoryService.searchCategories(searchDto, pageable);
        return ResponseEntity.ok(result);
    }


}
