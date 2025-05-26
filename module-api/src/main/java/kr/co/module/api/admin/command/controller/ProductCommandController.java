package kr.co.module.api.admin.command.controller;

import jakarta.validation.Valid;
import kr.co.module.api.admin.command.dto.ProductCreateDto;
import kr.co.module.api.admin.command.dto.ProductUpdateDto;
import kr.co.module.api.admin.command.service.ProductCommandService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product/command")
public class ProductCommandController {

    private final ProductCommandService productCommandService;

    public ProductCommandController(ProductCommandService productCommandService) {
        this.productCommandService = productCommandService;
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateDto productCreateDto) {
        boolean result = productCommandService.createProduct(productCreateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 등록 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_CREATE_FAIL.message(), ErrorCode.CATEGORY_CREATE_FAIL.code(), null)
            );
        }
    }

    // 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductUpdateDto productUpdateDto) {
        boolean result = productCommandService.updateProduct(productUpdateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 수정 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_NOT_FOUND.message(), ErrorCode.PRODUCT_NOT_FOUND.code(), null)
            );
        }
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@Valid @RequestBody ProductUpdateDto productUpdateDto) {
        boolean result = productCommandService.deleteProduct(productUpdateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 삭제 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_NOT_FOUND.message(), ErrorCode.PRODUCT_NOT_FOUND.code(), null)
            );
        }
    }
}
