package kr.co.module.api.admin.service;
import kr.co.module.api.admin.dto.*;
import kr.co.module.core.dto.domain.*;
import kr.co.module.mapper.repository.AdminReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminReservationService {

    private final MongoTemplate mongoTemplate;

    private final AdminReservationRepository adminReservationRepository;

    @Autowired
    public AdminReservationService(AdminReservationRepository adminReservationRepository, MongoTemplate mongoTemplate) {
        this.adminReservationRepository = adminReservationRepository;
        this.mongoTemplate = mongoTemplate;
    }
    // 예약 상태 변경
    public ReservationDto updateReservationStatus(AdminReservationUpdateDto updateDto) {
        return adminReservationRepository.findById(updateDto.getReservationId())
                .map(reservation -> {
                    reservation.setReservationStatus(updateDto.getReservationStatus());
                    reservation.setAmnrId(updateDto.getAdminId());
                    reservation.setAmndDttm(LocalDateTime.now());
                    return adminReservationRepository.save(reservation);
                })
                .orElse(null);
    }

    // 예약 정보 조회
    public List<ReservationDto> searchAdminReservations(AdminReservationSearchDto searchDto) {
        // 1. 상품 조건 동적 생성
        Criteria productCriteria = Criteria.where("crtrId").is(searchDto.getAdminId());
        if (searchDto.getProductId() == null || searchDto.getProductId().isEmpty()) {
            productCriteria.and("productId").is(searchDto.getProductId());
        }
        if (searchDto.getCategoryId() == null || searchDto.getCategoryId().isBlank()) {
            productCriteria.and("categoryId").is(searchDto.getCategoryId());
        }

        Query productQuery = new Query(productCriteria);
        List<ProductDto> myProducts = mongoTemplate.find(productQuery, ProductDto.class);

        List<String> myProductIds = myProducts.stream()
                .map(ProductDto::getProductId)
                .collect(Collectors.toList());

        if (myProductIds.isEmpty()) return Collections.emptyList();

        // 2. 예약 조건 생성
        Query reservationQuery = new Query(Criteria.where("productId").in(myProductIds));
        return mongoTemplate.find(reservationQuery, ReservationDto.class);
    }
}
