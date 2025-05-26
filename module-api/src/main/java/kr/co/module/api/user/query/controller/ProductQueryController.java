package kr.co.module.api.user.query.controller;
import kr.co.module.api.user.query.dto.ProductSearchDto;
import kr.co.module.api.user.query.service.ProductQueryService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/product/query")
public class ProductQueryController {
    private final ProductQueryService productQueryService;

    public ProductQueryController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }


    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @ModelAttribute ProductSearchDto searchDto
    ) {
        boolean hasAdminId = searchDto.getProductAdminId() != null;
        boolean hasCategoryId = searchDto.getCategoryId() != null;
        boolean hasPlace = searchDto.getProductPlace() != null && !searchDto.getProductPlace().isBlank();
        boolean hasDateRange = (searchDto.getSrchFromDate() != null && !searchDto.getSrchFromDate().isBlank())
                || (searchDto.getSrchToDate() != null && !searchDto.getSrchToDate().isBlank());

         if (!(hasAdminId || hasCategoryId || hasPlace || hasDateRange)) {
             System.out.println("hasAdminId = " + hasAdminId + ", hasCategoryId = " + hasCategoryId + ", hasPlace = " + hasPlace + ", hasDateRange = " + hasDateRange);

             return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.PRODUCT_REQUIRED_CONDITION.message(), ErrorCode.PRODUCT_REQUIRED_CONDITION.code(), null)
            );
        }
        List<ProductDto> result = productQueryService.searchProducts(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "상품 조회 성공", null));
    }

}
