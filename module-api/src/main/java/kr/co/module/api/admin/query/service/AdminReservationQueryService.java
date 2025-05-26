package kr.co.module.api.admin.query.service;
import kr.co.module.api.admin.query.dto.AdminReservationSearchDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.dto.domain.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminReservationQueryService {

    private final List<ReservationDto> reservationList;
    private final List<ProductDto> productList;

    public AdminReservationQueryService(List<ReservationDto> reservationList, List<ProductDto> productList) {
        this.reservationList = reservationList;
        this.productList = productList;
    }

    public List<ReservationDto> searchAdminReservations(AdminReservationSearchDto searchDto) {
        // 1. 관리자 ID로 본인 상품 ID 목록 추출
        List<Long> myProductIds = productList.stream()
                .filter(p -> p.getCrtrId().equals(searchDto.getAdminId()))
                .filter(p -> searchDto.getProductId() == 0 || p.getProductId().equals(searchDto.getProductId()))
                .filter(p -> searchDto.getCategoryId() == 0 || p.getCategoryId().equals(searchDto.getCategoryId()))
                .map(ProductDto::getProductId)
                .collect(Collectors.toList());

        // 2. 해당 상품들의 예약만 조회
        return reservationList.stream()
                .filter(r -> myProductIds.contains(r.getProductId()))
                .collect(Collectors.toList());
    }
}
