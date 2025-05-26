package kr.co.module.api.user.query.service;

import kr.co.module.api.user.query.dto.ProductSearchDto;
import kr.co.module.core.dto.domain.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductQueryService {
    private final List<ProductDto> productList;

    public ProductQueryService(List<ProductDto> productList) {
        this.productList = productList;
    }

    public List<ProductDto> searchProducts(ProductSearchDto searchDto) {
        return productList.stream()
                .filter(p -> !"Y".equals(p.getDltYsno()))
                // 관리자 ID 필터
                .filter(p -> searchDto.getProductAdminId() == null ||
                        (p.getCrtrId() != null && p.getCrtrId().equals(String.valueOf(searchDto.getProductAdminId()))))
                // 카테고리 ID 필터
                .filter(p -> searchDto.getCategoryId() == null ||
                        (p.getCategoryId() != null && p.getCategoryId().equals(searchDto.getCategoryId())))
                // 상품명 필터
                .filter(p -> searchDto.getProductName() == null ||
                        (p.getProductName() != null && p.getProductName().contains(searchDto.getProductName())))
                // 장소 필터
                .filter(p -> searchDto.getProductPlace() == null ||
                        (p.getProductPlace() != null && p.getProductPlace().contains(searchDto.getProductPlace())))
                // 날짜 범위 필터
                .filter(p -> {
                    if (searchDto.getSrchFromDate() == null && searchDto.getSrchToDate() == null) return true;
                    // 상품의 이용 가능 날짜 리스트가 하나라도 범위에 포함되면 통과
                    if (p.getProductAvlbDateList() == null) return false;
                    return p.getProductAvlbDateList().stream().anyMatch(date ->
                            (searchDto.getSrchFromDate() == null || date.compareTo(searchDto.getSrchFromDate()) >= 0) &&
                                    (searchDto.getSrchToDate() == null || date.compareTo(searchDto.getSrchToDate()) <= 0));
                })
                // 시간 범위 필터
                .filter(p -> {
                    if (searchDto.getSrchFromTime() == null && searchDto.getSrchToTime() == null) return true;
                    if (p.getProductAvlbTimeSlots() == null) return false;
                    // 날짜별로 시간대가 하나라도 범위에 포함되면 통과
                    return p.getProductAvlbTimeSlots().values().stream().flatMap(List::stream).anyMatch(time ->
                            (searchDto.getSrchFromTime() == null || time.compareTo(searchDto.getSrchFromTime()) >= 0) &&
                                    (searchDto.getSrchToTime() == null || time.compareTo(searchDto.getSrchToTime()) <= 0));
                })
                .collect(Collectors.toList());
    }
}
