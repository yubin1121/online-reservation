package kr.co.module.api.user.service;
import com.mongodb.client.result.UpdateResult;
import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.dto.ReservationSearchDto;
import kr.co.module.api.user.dto.ReservationUpdateDto;
import kr.co.module.core.domain.Reservation;
import kr.co.module.core.domain.Product;
import kr.co.module.core.exception.InsufficientStockException;
import kr.co.module.core.exception.ProductNotFoundException;
import kr.co.module.core.exception.ReservationNotFoundException;
import kr.co.module.core.status.ReservationStatus;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.UserReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final AdminProductRepository productRepository;
    private final UserReservationRepository reservationRepository;
    private final MongoTemplate mongoTemplate;

    private void validateReservation(Product product) {
        if ("Y".equals(product.getDltYsno())) {
            throw new IllegalStateException("삭제된 상품입니다");
        }
    }

    private void validateOwnership(Reservation reservation, String userId) {
        if (!reservation.getUserId().equals(userId)) {
            throw new SecurityException("예약 소유자가 아닙니다");
        }
    }

    private void validateStatus(Reservation reservation, EnumSet<ReservationStatus> allowed) {
        ReservationStatus status = ReservationStatus.valueOf(reservation.getReservationStatus());
        if (!allowed.contains(status)) {
            throw new IllegalStateException("허용되지 않은 상태: " + status);
        }
    }

    private void handleStockChange(Reservation reservation, Integer newCount) {
        if (newCount != null && !newCount.equals(reservation.getReservationCnt())) {
            int delta = newCount - reservation.getReservationCnt();
            updateProductStock(reservation.getProductId(), -delta);
            reservation.setReservationCnt(newCount);
        }
    }

    private void updateProductStock(String productId, int quantityDelta) {
        //Query query = new Query(Criteria.where("_id").is(productId));
        //Update update = new Update().inc("totalQuantity", quantityDelta);
        //mongoTemplate.updateFirst(query, update, ProductDto.class);
        // 재고 0 이상일 경우 업데이트 가능.
        Query query = new Query(
                Criteria.where("_id").is(productId)
                        .and("totalQuantity").gte(Math.abs(quantityDelta))
        );
        Update update = new Update().inc("totalQuantity", quantityDelta);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);

        if (result.getModifiedCount() == 0) {
            Product latestProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId + " (재고 업데이트 실패 시점)")); // 상품이 아예 없는 경우

            throw new InsufficientStockException(
                    "재고 부족 또는 업데이트 실패: 요청 " + Math.abs(quantityDelta) +
                            "/현재 " + latestProduct.getTotalQuantity() +
                            " (상품 ID: " + productId + ")"
            );
        }
    }

    private Reservation buildReservation(ReservationRequestDto dto, Product product) {
        return Reservation.builder()
                .id(null)
                .reservationBizId(dto.getProductId()+dto.getUserId()+dto.getReservationDate()+dto.getReservationTime())
                .productId(dto.getProductId())
                .productCategoryId(product.getCategoryId())
                .reservationStatus("PENDING")
                .reservationTime(dto.getReservationTime())
                .reservationDate(dto.getReservationDate())
                .reservationCnt(dto.getReservationCnt())
                .crtrId(dto.getUserId())
                .cretDttm(LocalDateTime.now())
                .amnrId(dto.getUserId())
                .amndDttm(LocalDateTime.now())
                .dltYsno("N")
                .build();
    }

    private void validateStock(Product product, int required) {
        log.debug("재고 업데이트: productId={}, quantity={}, required={}",
                product.getId(), product.getTotalQuantity(), required);
        if (product.getTotalQuantity() < required) {
            throw new InsufficientStockException(
                    "재고 부족: 요청 " + required + "/현재 " + product.getTotalQuantity()
            );
        }
    }

    // 예약 신청
    @Transactional
    public Reservation reserve(ReservationRequestDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        validateStock(product, request.getReservationCnt());
        validateReservation(product);

        Reservation reservation = buildReservation(request, product);
        reservationRepository.save(reservation);

        updateProductStock(product.getId(), -request.getReservationCnt());
        log.info("예약 생성: {}", reservation);
        return reservation;
    }

    private void updateReservationDetails(Reservation reservation, ReservationUpdateDto updateDto) {
        // 1. 날짜 업데이트
        if (updateDto.getReservationDate() != null) {
            reservation.setReservationDate(updateDto.getReservationDate());
        }

        // 2. 시간 업데이트
        if (updateDto.getReservationTime() != null) {
            reservation.setReservationTime(updateDto.getReservationTime());
        }

        // 3. 상태 업데이트
        if (updateDto.getReservationStatus() != null) {
            ReservationStatus currentStatus = ReservationStatus.valueOf(reservation.getReservationStatus());
            ReservationStatus newStatus = ReservationStatus.valueOf(updateDto.getReservationStatus());

            // 새로운 상태가 현재 상태와 다를 때
            if (!newStatus.equals(currentStatus)) {
                 if (!currentStatus.canTransitionTo(newStatus)) {
                     throw new IllegalStateException("허용되지 않은 상태 전환: " + currentStatus + " -> " + newStatus);
                 }
                reservation.setReservationStatus(newStatus.name());
            }
        }

        // 4. 수정자 정보 업데이트 (항상 수행)
        reservation.setAmnrId(updateDto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());
    }

    // 예약 변경
    @Transactional
    public Reservation updateReservation(ReservationUpdateDto dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        validateOwnership(reservation, dto.getUserId());
        validateStatus(reservation, EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        Integer oldReservationCnt = reservation.getReservationCnt();

        updateReservationDetails(reservation, dto);

        if (dto.getReservationCnt() != null && !dto.getReservationCnt().equals(oldReservationCnt)) {
            int newReservationCnt = dto.getReservationCnt();
            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(reservation.getProductId()));

            // 2. 변경에 필요한 순수 재고량
            int stockAfterChange = product.getTotalQuantity() + oldReservationCnt - newReservationCnt;

            // 3. 재고 부족 검증:
            if (stockAfterChange < 0) {
                throw new InsufficientStockException(
                        "예약 수량 변경 불가: 요청 " + newReservationCnt + "개로 변경 시 재고 부족. " +
                                "현재 유효 재고: " + (product.getTotalQuantity() + oldReservationCnt) + "개"
                );
            }

            // 4. 재고 업데이트
            int delta = newReservationCnt - oldReservationCnt;
            updateProductStock(reservation.getProductId(), -delta); // 재고 감소는 -delta, 증가는 +delta

            // 5. Reservation 객체의 예약 수량 업데이트
            reservation.setReservationCnt(newReservationCnt);
          }

        log.info("예약 수정: {}", reservation);
        return reservationRepository.save(reservation);
    }

    // 예약 취소
    @Transactional
    public Reservation cancelReservation(ReservationUpdateDto dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        validateOwnership(reservation, dto.getUserId());
        validateStatus(reservation, EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        updateProductStock(reservation.getProductId(), reservation.getReservationCnt()); // 재고 복구

        reservation.setReservationStatus(ReservationStatus.CANCELED.toString());
        reservation.setAmnrId(dto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());

        log.info("예약 취소: {}", reservation);
        return reservationRepository.save(reservation);
    }

    // 사용자 예약 목록 검색

    public List<Reservation> searchUserReservations(ReservationSearchDto searchDto) {
        Criteria criteria = Criteria.where("userId").is(searchDto.getUserId());

        if (searchDto.getCategoryId() != null) {
            criteria.and("productCategoryId").is(searchDto.getCategoryId()); // (1)
        }

        if (searchDto.getSrchFromDate() != null && searchDto.getSrchToDate() != null) {
            criteria.and("reservationDate").gte(searchDto.getSrchFromDate())
                    .lte(searchDto.getSrchToDate());
        }

        Query query = new Query(criteria);
        List<Reservation> reservations = mongoTemplate.find(query, Reservation.class); // (2)

        return reservations;
    }
}
