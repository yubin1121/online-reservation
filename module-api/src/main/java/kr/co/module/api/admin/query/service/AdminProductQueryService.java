package kr.co.module.api.admin.query.service;

import kr.co.module.api.admin.query.dto.AdminProductSearchDto;
import kr.co.module.core.dto.domain.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminProductQueryService {

    private final List<ProductDto> productList;

    // 생성자 주입 (테스트/개발 환경에서는 커맨드 서비스에서 리스트를 받아올 수도 있음)
    public AdminProductQueryService(List<ProductDto> productList) {
        this.productList = productList;
    }

    // 본인(관리자) 상품만 조회
    public List<ProductDto> searchMyProducts(AdminProductSearchDto searchDto) {
        return productList.stream()
                .filter(p -> p.getCrtrId().equals(searchDto.getAdminId()))
                .filter(p -> !"Y".equals(p.getDltYsno())) // 삭제 안 된 상품만
                .collect(Collectors.toList());
    }
}