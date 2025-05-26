package kr.co.module.api.user.query.service;

import kr.co.module.api.user.query.dto.ReservationSearchDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.dto.domain.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationQueryService {

    private final List<ReservationDto> reservationList;
    private final List<ProductDto> productList; // 카테고리ID 필터링용

    public ReservationQueryService(List<ReservationDto> reservationList, List<ProductDto> productList) {
        this.reservationList = reservationList;
        this.productList = productList;
    }

    public List<ReservationDto> searchUserReservations(ReservationSearchDto searchDto) {
        return reservationList.stream()
                .filter(r -> r.getUserId().equals(String.valueOf(searchDto.getUserId())))
                .filter(r -> {
                    // 카테고리 ID로 필터링
                    if (searchDto.getCategoryId() == null) return true;
                    return productList.stream()
                            .anyMatch(p -> p.getProductId().equals(r.getProductId()) &&
                                    p.getCategoryId().equals(searchDto.getCategoryId()));
                })
                .filter(r -> r.getReservationDate().compareTo(searchDto.getSrchFromDate()) >= 0 &&
                        r.getReservationDate().compareTo(searchDto.getSrchToDate()) <= 0)
                .collect(Collectors.toList());
    }
}