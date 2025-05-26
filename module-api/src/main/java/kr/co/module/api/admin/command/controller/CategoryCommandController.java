package kr.co.module.api.admin.command.controller;

import jakarta.validation.Valid;
import kr.co.module.api.admin.command.dto.CategoryCreateDto;
import kr.co.module.api.admin.command.dto.CategoryUpdateDto;
import kr.co.module.api.admin.command.service.CategoryCommandService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category/command")
public class CategoryCommandController {

    private final CategoryCommandService categoryCommandService;

    public CategoryCommandController(CategoryCommandService categoryCommandService) {
        this.categoryCommandService = categoryCommandService;
    }

    // 카테고리 등록
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        boolean result = categoryCommandService.createCategory(categoryCreateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "카테고리 등록 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.CATEGORY_CREATE_FAIL.message(), ErrorCode.CATEGORY_CREATE_FAIL.code(), null)
            );
        }
    }

    // 카테고리 수정
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoryUpdateDto categoryUpdateDto) {
        boolean result = categoryCommandService.updateCategory(categoryUpdateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "카테고리 수정 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.CATEGORY_NOT_FOUND.message(), ErrorCode.CATEGORY_NOT_FOUND.code(), null)
            );
        }
    }

    // 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@Valid @RequestBody CategoryUpdateDto categoryUpdateDto) {
        boolean result = categoryCommandService.deleteCategory(categoryUpdateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "카테고리 삭제 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.CATEGORY_NOT_FOUND.message(), ErrorCode.CATEGORY_NOT_FOUND.code(), null)
            );
        }
    }
}
