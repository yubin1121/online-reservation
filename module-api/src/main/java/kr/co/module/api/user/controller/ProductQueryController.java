package kr.co.module.api.user.controller;
import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.api.user.service.ProductQueryService;
import kr.co.module.core.code.ErrorCode;
import kr.co.module.core.domain.Product;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/product")
public class ProductQueryController {
    private final ProductQueryService productQueryService;


    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<ApiResponse<List<Product>>>>  searchProducts(
            @ModelAttribute ProductSearchDto searchDto
    ) {
        boolean hasAdminId = searchDto.getProductAdminId() != null;
        boolean hasCategoryId = searchDto.getCategoryId() != null;
        boolean hasPlace = searchDto.getProductPlace() != null && !searchDto.getProductPlace().isBlank();
        boolean hasDateRange = (searchDto.getSrchFromDate() != null && !searchDto.getSrchFromDate().isBlank())
                || (searchDto.getSrchToDate() != null && !searchDto.getSrchToDate().isBlank());

        if (!(hasAdminId || hasCategoryId || hasPlace || hasDateRange)) {
            log.info("hasAdminId = {}, hasCategoryId = {}, hasPlace = {}, hasDateRange = {}",
                    hasAdminId, hasCategoryId, hasPlace, hasDateRange);

            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                            new ApiResponse<>(false, null, null,
                                    new ErrorResponse(
                                            ErrorCode.PRODUCT_REQUIRED_CONDITION.message(),
                                            ErrorCode.PRODUCT_REQUIRED_CONDITION.code()))
                    )
            );
        }

        return productQueryService.searchProducts(searchDto)
                .thenApply(products ->
                        ResponseEntity.ok(
                                new ApiResponse<>(true, products, "상품 조회 성공", null)
                        ))
                .exceptionally(ex -> {
                    log.error("상품 검색 중 오류 발생: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().body(
                            new ApiResponse<>(false, null, null,
                                    new ErrorResponse(
                                            "상품 검색 실패",
                                            "PRODUCT_SEARCH_ERROR"
                                    )
                            )
                    );
                });
    }

}
