package kr.co.module.api.admin.query.controller;

import kr.co.module.api.admin.query.dto.AdminProductSearchDto;
import kr.co.module.api.admin.query.service.AdminProductQueryService;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/query")
public class AdminProductQueryController {

    private final AdminProductQueryService adminProductQueryService;

    public AdminProductQueryController(AdminProductQueryService adminProductQueryService) {
        this.adminProductQueryService = adminProductQueryService;
    }

    // 본인(관리자) 상품만 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchMyProducts(
            @ModelAttribute AdminProductSearchDto searchDto
    ) {
        List<ProductDto> result = adminProductQueryService.searchMyProducts(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "본인 상품 조회 성공", null));
    }
}
