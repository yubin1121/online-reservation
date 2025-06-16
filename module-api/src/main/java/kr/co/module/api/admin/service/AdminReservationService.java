package kr.co.module.api.admin.service;
import kr.co.module.api.admin.dto.*;
import kr.co.module.core.dto.domain.*;
import kr.co.module.core.exception.ReservationNotFoundException;
import kr.co.module.core.exception.UnauthorizedAccessException;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.AdminReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final MongoTemplate mongoTemplate;

    private final AdminReservationRepository adminReservationRepository;
    private final AdminProductRepository adminProductRepository;

    // 예약 상태 변경
    public ReservationDto updateReservationStatus(AdminReservationUpdateDto dto) {
        ReservationDto reservation = adminReservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        // 본인 상품의 예약인지 확인
        validateReservationOwnership(reservation.getProductId(), dto.getAdminId());

        reservation.setReservationStatus(dto.getReservationStatus());
        reservation.setAmnrId(dto.getAdminId());
        reservation.setAmndDttm(LocalDateTime.now());

        log.info("Reservation {} status updated to {} by admin {}",
                dto.getReservationId(), dto.getReservationStatus(), dto.getAdminId());

        return adminReservationRepository.save(reservation);
    }

    private void validateReservationOwnership(String productId, String adminId) {
        if (!adminProductRepository.existsByProductIdAndCrtrId(productId, adminId)) {
            throw new UnauthorizedAccessException("해당 상품에 대한 권한이 없습니다");
        }
    }


    // 2. 예약 검색
    public List<ReservationDto> searchAdminReservations(AdminReservationSearchDto searchDto) {
        // 본인 상품 ID 목록 조회
        List<String> productIds = getMyProductIds(searchDto);

        // 예약 검색 쿼리 빌드
        Criteria criteria = Criteria.where("productId").in(productIds);
        if (StringUtils.hasText(searchDto.getProductId())) {
            criteria.and("productId").is(searchDto.getProductId());
        }

        // 3. 카테고리 ID 조건
        if (StringUtils.hasText(searchDto.getCategoryId())) {
            criteria.and("categoryId").is(searchDto.getCategoryId());
        }

        // 4. 예약 상태 조건
        if (StringUtils.hasText(searchDto.getReservationStatus())) {
            criteria.and("reservationStatus").is(searchDto.getReservationStatus());
        }

        // 5. 날짜 범위 조건
        if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
            criteria.and("reservationDate")
                    .gte(searchDto.getFromDate())
                    .lte(searchDto.getToDate());
        } else {
            if (searchDto.getFromDate() != null) {
                criteria.and("reservationDate").gte(searchDto.getFromDate());
            }
            if (searchDto.getToDate() != null) {
                criteria.and("reservationDate").lte(searchDto.getToDate());
            }
        }

        List<ProductDto> products = mongoTemplate.find(new Query(criteria), ProductDto.class);
        // 상품이 없으면 바로 빈 리스트 반환
        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        Query reservationQuery = new Query(Criteria.where("productId").in(productIds));

        return mongoTemplate.find(reservationQuery, ReservationDto.class);

    }

    // 3. 본인 상품 ID 조회
    private List<String> getMyProductIds(AdminReservationSearchDto searchDto) {
        Criteria productCriteria = Criteria.where("crtrId").is(searchDto.getAdminId());

        if (StringUtils.hasText(searchDto.getProductId())) {
            productCriteria.and("productId").is(searchDto.getProductId());
        }
        if (StringUtils.hasText(searchDto.getCategoryId())) {
            productCriteria.and("categoryId").is(searchDto.getCategoryId());
        }

        return mongoTemplate.find(new Query(productCriteria), ProductDto.class)
                .stream()
                .map(ProductDto::getProductId)
                .collect(Collectors.toList());
    }


}
