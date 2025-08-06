package kr.co.module.api.admin.service;
import com.mongodb.client.result.UpdateResult;
import kr.co.module.api.admin.dto.*;
import kr.co.module.core.domain.Product;
import kr.co.module.core.domain.Reservation;
import kr.co.module.core.exception.InsufficientStockException;
import kr.co.module.core.exception.ProductNotFoundException;
import kr.co.module.core.exception.ReservationNotFoundException;
import kr.co.module.core.exception.UnauthorizedAccessException;
import kr.co.module.core.status.ReservationStatus;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.AdminReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final MongoTemplate mongoTemplate;

    private final AdminReservationRepository adminReservationRepository;
    private final AdminProductRepository adminProductRepository;

    private void updateProductStock(String productId, int quantityDelta) {
        Query query = new Query(
                Criteria.where("_id").is(productId)
                        .and("totalQuantity").gte(Math.abs(quantityDelta))
        );
        Update update = new Update().inc("totalQuantity", quantityDelta);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);

        if (result.getModifiedCount() == 0) {
            Product latestProduct = adminProductRepository.findById(productId) // adminProductRepository 사용
                    .orElseThrow(() -> new ProductNotFoundException(productId + " (재고 업데이트 실패 시점)"));

            throw new InsufficientStockException(
                    "재고 부족 또는 업데이트 실패: 요청 " + Math.abs(quantityDelta) +
                            "/현재 " + latestProduct.getTotalQuantity() +
                            " (상품 ID: " + productId + ")"
            );
        }
    }

    // 1-1.예약 상태 변경
    @Transactional
    public Reservation updateReservationStatus(AdminReservationUpdateDto dto) {
        Reservation reservation = adminReservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        // 본인 상품의 예약인지 확인
        validateReservationOwnership(reservation.getProductId(), dto.getAdminId());

        ReservationStatus currentStatus = ReservationStatus.valueOf(reservation.getReservationStatus());
        ReservationStatus newStatus = ReservationStatus.valueOf(dto.getReservationStatus());

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "허용되지 않은 상태 전환: " + currentStatus.name() + " -> " + newStatus.name() +
                            " (예약 ID: " + reservation.getId() + ")"
            );
        }

        reservation.setReservationStatus(newStatus.name());
        reservation.setAmnrId(dto.getAdminId());
        reservation.setAmndDttm(LocalDateTime.now());


        log.info("Reservation {} status updated to {} by admin {}",
                dto.getReservationId(), newStatus.name(), dto.getAdminId());

        return adminReservationRepository.save(reservation);
    }

    private void validateReservationOwnership(String id, String adminId) {
        if (!adminProductRepository.existsByIdAndCrtrId(id, adminId)) {
            throw new UnauthorizedAccessException("해당 상품에 대한 권한이 없습니다");
        }
    }

    // 1-2.예약 거절
    @Transactional
    public Reservation rejectReservation(AdminReservationUpdateDto dto) {
        Reservation reservation = adminReservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        // 본인 상품의 예약인지 확인
        validateReservationOwnership(reservation.getProductId(), dto.getAdminId());

        // PENDING 상태의 예약만 거절 가능하도록 검증
        ReservationStatus currentStatus = ReservationStatus.valueOf(reservation.getReservationStatus());
        if (!currentStatus.canTransitionTo(ReservationStatus.REJECTED)) {
            throw new IllegalStateException(
                    "허용되지 않은 상태 전환: " + currentStatus.name() + " -> " + ReservationStatus.REJECTED.name() +
                            " (예약 ID: " + reservation.getId() + ")"
            );
        }

        // 재고 복구
        updateProductStock(reservation.getProductId(), reservation.getReservationCnt());

        // 예약 상태를 REJECTED로 변경 및 수정자 정보 업데이트
        reservation.setReservationStatus(ReservationStatus.REJECTED.name());
        reservation.setAmnrId(dto.getAdminId());
        reservation.setAmndDttm(LocalDateTime.now());

        log.info("예약 거절: {} (관리자: {})", reservation.getId(), dto.getAdminId());
        return adminReservationRepository.save(reservation);
    }


    @Qualifier("adminQueryExecutor")
    private final Executor adminQueryExecutor;

    // 2. 예약 검색
    public CompletableFuture<List<Reservation>> searchAdminReservations(AdminReservationSearchDto searchDto) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. 본인 상품 ID 목록 조회
            List<String> myProductIds = getMyProductIds(searchDto);

            if (myProductIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 예약 검색 쿼리 빌드
            Criteria reservationCriteria = Criteria.where("productId").in(myProductIds);

            // 3. 카테고리 ID 조건
            if (StringUtils.hasText(searchDto.getCategoryId())) {
                reservationCriteria.and("productCategoryId").is(searchDto.getCategoryId());
            }

            // 4. 예약 상태 조건
            if (StringUtils.hasText(searchDto.getReservationStatus())) {
                reservationCriteria.and("reservationStatus").is(searchDto.getReservationStatus());
            }

            // 5. 날짜 범위 조건
            if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
                reservationCriteria.and("reservationDate")
                        .gte(searchDto.getFromDate())
                        .lte(searchDto.getToDate());
            } else {
                if (searchDto.getFromDate() != null) {
                    reservationCriteria.and("reservationDate").gte(searchDto.getFromDate());
                }
                if (searchDto.getToDate() != null) {
                    reservationCriteria.and("reservationDate").lte(searchDto.getToDate());
                }
            }

            Query reservationQuery = new Query(reservationCriteria);
            return mongoTemplate.find(reservationQuery, Reservation.class);

        }, adminQueryExecutor); // 👈 지정된 executor에서 실행
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

        return mongoTemplate.find(new Query(productCriteria), Product.class)
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());
    }

    /**
     * Kafka 이벤트 메시지로 수신한 예약 처리 메서드\
     */
    @Transactional
    public void handleNewReservationEvent(Reservation incomingReservation) {
        try {
            // 1. 같은 reservationBizId 예약이 이미 있는지 조회
            Optional<Reservation> existingOpt = adminReservationRepository.findByReservationBizId(incomingReservation.getReservationBizId());

            if (existingOpt.isPresent()) {
                Reservation existing = existingOpt.get();

                // 2. 기존 상태와 비교 후 업데이트 필요 시 처리
                if (!existing.getReservationStatus().equals(incomingReservation.getReservationStatus())) {
                    existing.setReservationStatus(incomingReservation.getReservationStatus());
                    existing.setAmnrId(incomingReservation.getAmnrId());
                    existing.setAmndDttm(LocalDateTime.now());
                    adminReservationRepository.save(existing);
                    log.info("기존 예약 상태 업데이트: {}", existing.getId());
                } else {
                    log.info("예약 상태 변경 없음: {}", existing.getId());
                }
            } else {
                // 3. 없는 예약은 신규 저장
                adminReservationRepository.save(incomingReservation);
                log.info("신규 예약 이벤트 저장: {}", incomingReservation.getId());
            }

        } catch (Exception e) {
            log.error("handleNewReservationEvent 처리 중 오류 발생", e);
        }
    }
}
