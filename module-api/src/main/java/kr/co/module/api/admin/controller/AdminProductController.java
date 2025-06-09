package kr.co.module.api.admin.controller;

import jakarta.validation.Valid;
import kr.co.module.api.admin.dto.*;
import kr.co.module.api.admin.service.AdminProductService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

    private final AdminProductService adminProductService;


    // 상품 등록
    @PostMapping("register")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateDto productCreateDto) {
        ProductDto result = adminProductService.createProduct(productCreateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 등록 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_CREATE_FAIL.message(), ErrorCode.CATEGORY_CREATE_FAIL.code(), null)
            );
        }
    }

    // 상품 수정
    @PutMapping("update/{productId}")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductUpdateDto productUpdateDto) {
        ProductDto result = adminProductService.updateProduct(productUpdateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 수정 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_NOT_FOUND.message(), ErrorCode.PRODUCT_NOT_FOUND.code(), null)
            );
        }
    }

    // 상품 삭제
    @DeleteMapping("delete/{productId}")
    public ResponseEntity<?> deleteProduct(@Valid @RequestBody ProductUpdateDto productUpdateDto) {
        ProductDto result = adminProductService.deleteProduct(productUpdateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 삭제 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_NOT_FOUND.message(), ErrorCode.PRODUCT_NOT_FOUND.code(), null)
            );
        }
    }

    // 본인(관리자) 상품만 조회
    @GetMapping("my")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchMyProducts(
            @ModelAttribute AdminProductSearchDto searchDto
    ) {
        List<ProductDto> result = adminProductService.searchMyProducts(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "본인 상품 조회 성공", null));
    }
}
