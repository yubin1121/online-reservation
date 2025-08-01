package kr.co.module.api.admin.controller;

import jakarta.validation.Valid;
import kr.co.module.api.admin.dto.*;
import kr.co.module.api.admin.service.AdminProductService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.domain.Product;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

    private final AdminProductService adminProductService;

    // 상품 등록
    @PostMapping(value = "register", consumes = {"multipart/form-data"})
    public CompletableFuture<ResponseEntity<?>> createProduct(
            @Valid @RequestPart("productCreateDto") ProductCreateDto productCreateDto,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages) {

        productCreateDto.setProductImages(productImages);

        return adminProductService.createProduct(productCreateDto)
                .thenApply(result ->
                        result.map(p -> ResponseEntity.ok(new ApiResponse<>(true, null, "상품 등록 성공", null)))
                                .orElseGet(() -> ResponseEntity.badRequest().body(
                                        new ApiResponse<>(false, null, null,
                                                new ErrorResponse(ErrorCode.PRODUCT_CREATE_FAIL.message(), ErrorCode.CATEGORY_CREATE_FAIL.code()))
                                ))
                );
    }

    // 상품 수정
    @PutMapping(value = "update/{productId}", consumes = {"multipart/form-data"})
    public CompletableFuture<ResponseEntity<ApiResponse<Object>>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestPart("productUpdateDto") ProductUpdateDto productUpdateDto,
            @RequestPart(value = "newProductImages", required = false) List<MultipartFile> newProductImages) {

        productUpdateDto.setProductId(productId);
        productUpdateDto.setNewProductImages(newProductImages);

        return adminProductService.updateProduct(productUpdateDto)
                .thenApply(result ->
                        result.map(p -> ResponseEntity.ok(new ApiResponse<>(true, null, "상품 수정 성공", null)))
                                .orElseGet(() -> ResponseEntity.badRequest().body(
                                        new ApiResponse<>(false, null, null,
                                                new ErrorResponse(ErrorCode.PRODUCT_NOT_FOUND.message(), ErrorCode.PRODUCT_NOT_FOUND.code()))
                                ))
                );
    }

    // 상품 삭제
    @DeleteMapping("delete/{productId}")
    public CompletableFuture<ResponseEntity<ApiResponse<Object>>> deleteProduct(@Valid @RequestBody ProductUpdateDto dto) {
        return adminProductService.deleteProduct(dto)
                .thenApply(result ->
                        result.map(p -> ResponseEntity.ok(new ApiResponse<>(true, null, "상품 삭제 성공", null)))
                                .orElseGet(() -> ResponseEntity.badRequest().body(
                                        new ApiResponse<>(false, null, null,
                                                new ErrorResponse(ErrorCode.PRODUCT_NOT_FOUND.message(), ErrorCode.PRODUCT_NOT_FOUND.code()))
                                ))
                );
    }


    // 본인(관리자) 상품만 조회
    @GetMapping("my")
    public CompletableFuture<ResponseEntity<ApiResponse<List<Product>>>> searchMyProducts(
            @ModelAttribute AdminProductSearchDto searchDto
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<Product> result = adminProductService.searchMyProducts(searchDto);
            return ResponseEntity.ok(new ApiResponse<>(true, result, "본인 상품 조회 성공", null));
        });
    }
}
