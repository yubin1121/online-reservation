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

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

    private final AdminProductService adminProductService;


    // 상품 등록
    @PostMapping(value = "register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createProduct(
            @Valid @RequestPart("productCreateDto") ProductCreateDto productCreateDto, // <-- RequestPart로 DTO 받기
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages) { // <-- 이미지 파일 받기 {

        productCreateDto.setProductImages(productImages);
        Product result = adminProductService.createProduct(productCreateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "상품 등록 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_CREATE_FAIL.message(), ErrorCode.CATEGORY_CREATE_FAIL.code(), null)
            );
        }
    }

    // 상품 수정
    @PutMapping(value = "update/{productId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProduct(
            @PathVariable String productId,
            @Valid @RequestPart("productUpdateDto") ProductUpdateDto productUpdateDto, // <-- RequestPart로 DTO 받기
            @RequestPart(value = "newProductImages", required = false) List<MultipartFile> newProductImages) { // <-- 새로운 이미지 파일 받기

        productUpdateDto.setProductId(productId); // PathVariable로 받은 productId를 DTO에 설정
        productUpdateDto.setNewProductImages(newProductImages); // 새로운 이미지 파일 리스트 설정

        Product result = adminProductService.updateProduct(productUpdateDto);
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
        Product result = adminProductService.deleteProduct(productUpdateDto);
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
    public ResponseEntity<ApiResponse<List<Product>>> searchMyProducts(
            @ModelAttribute AdminProductSearchDto searchDto
    ) {
        List<Product> result = adminProductService.searchMyProducts(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "본인 상품 조회 성공", null));
    }
}
