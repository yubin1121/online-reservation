package kr.co.module.api.user.service;
import com.mongodb.client.result.UpdateResult;
import kr.co.module.api.admin.dto.CategoryCreateDto;
import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.dto.ReservationSearchDto;
import kr.co.module.api.user.dto.ReservationUpdateDto;
import kr.co.module.core.dto.domain.CategoryDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.exception.InsufficientStockException;
import kr.co.module.core.exception.ProductNotFoundException;
import kr.co.module.core.exception.ReservationNotFoundException;
import kr.co.module.core.status.ReservationStatus;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.UserReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
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

    private void validateReservation(ProductDto product, int count) {
        if ("Y".equals(product.getDltYsno())) {
            throw new IllegalStateException("삭제된 상품입니다");
        }
        if (product.getTotalQuantity() < count) {
            throw new InsufficientStockException(
                    "재고 부족: 요청 " + count + "/현재 " + product.getTotalQuantity()
            );
        }
    }

    private void validateOwnership(ReservationDto reservation, String userId) {
        if (!reservation.getUserId().equals(userId)) {
            throw new SecurityException("예약 소유자가 아닙니다");
        }
    }

    private void validateStatus(ReservationDto reservation, EnumSet<ReservationStatus> allowed) {
        ReservationStatus status = ReservationStatus.valueOf(reservation.getReservationStatus());
        if (!allowed.contains(status)) {
            throw new IllegalStateException("허용되지 않은 상태: " + status);
        }
    }

    private void handleStockChange(ReservationDto reservation, Integer newCount) {
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
        UpdateResult result = mongoTemplate.updateFirst(query, update, ProductDto.class);

        if (result.getModifiedCount() == 0) {
            throw new InsufficientStockException(
                    "재고 부족: 요청 " + result.getModifiedCount() + "/현재 " + quantityDelta
            );
        }
    }

    private ReservationDto buildReservation(ReservationRequestDto dto) {
        return ReservationDto.builder()
                .reservationId(dto.getProductId()+dto.getUserId()+dto.getReservationDate()+dto.getReservationTime())
                .reservationStatus("PENDING")
                .reservationTime(dto.getReservationTime())
                .reservationDate(dto.getReservationDate())
                .reservationCnt(dto.getReservationCnt())
                .crtrId(dto.getUserId())
                .amnrId(dto.getUserId())
                .dltYsno("N")
                .build();
    }

    // 예약 신청
    @Transactional
    public ReservationDto reserve(ReservationRequestDto request) {
        ProductDto product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        validateReservation(product, request.getReservationCnt());

        ReservationDto reservation = buildReservation(request);
        reservationRepository.save(reservation);

        updateProductStock(product.getProductId(), -request.getReservationCnt());
        log.info("예약 생성: {}", reservation);
        return reservation;
    }

    private void updateReservationDetails(ReservationDto reservation, ReservationUpdateDto updateDto) {
        // 1. 날짜 업데이트
        if (updateDto.getReservationDate() != null) {
            reservation.setReservationDate(updateDto.getReservationDate());
        }

        // 2. 시간 업데이트
        if (updateDto.getReservationTime() != null) {
            reservation.setReservationTime(updateDto.getReservationTime());
        }

        // 3. 상태 업데이트 (상태 코드 유효성 검사 후)
        if (updateDto.getReservationStatus() != null) {
            ReservationStatus newStatus = ReservationStatus.valueOf(updateDto.getReservationStatus());
            if (newStatus.toString().equals(reservation.getReservationStatus())) {
                reservation.setReservationStatus(newStatus.name());
            }
        }

        // 4. 수정자 정보 업데이트 (항상 수행)
        reservation.setAmnrId(updateDto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());
    }

    // 예약 변경
    public ReservationDto updateReservation(ReservationUpdateDto dto) {
        ReservationDto reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        validateOwnership(reservation, dto.getUserId());
        validateStatus(reservation, EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        updateReservationDetails(reservation, dto);
        handleStockChange(reservation, dto.getReservationCnt());

        log.info("예약 수정: {}", reservation);
        return reservationRepository.save(reservation);
    }

    // 예약 취소
    public ReservationDto cancelReservation(ReservationUpdateDto dto) {
        ReservationDto reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(dto.getReservationId()));

        validateOwnership(reservation, dto.getUserId());
        validateStatus(reservation, EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        reservation.setReservationStatus(ReservationStatus.CANCELED.toString());
        updateProductStock(reservation.getProductId(), reservation.getReservationCnt());

        log.info("예약 취소: {}", reservation);
        return reservationRepository.save(reservation);
    }

    // 사용자 예약 목록 검색
    public List<ReservationDto> searchUserReservations(ReservationSearchDto searchDto) {
        Criteria criteria = Criteria.where("userId").is(searchDto.getUserId());

        // 카테고리 ID로 필터링 (Product 컬렉션 조인 불가, DB에서 먼저 예약 조회 후 Java에서 필터링)
        Query query = new Query(criteria);
        List<ReservationDto> reservations = mongoTemplate.find(query, ReservationDto.class);

        // 카테고리 ID 필터 (ProductDto를 조회해서 매칭)
        if (searchDto.getCategoryId() != null) {
            reservations = reservations.stream()
                    .filter(r -> {
                        Optional<ProductDto> productOpt = productRepository.findById(r.getProductId());
                        return productOpt.isPresent() &&
                                searchDto.getCategoryId().equals(productOpt.get().getCategoryId());
                    })
                    .toList();
        }

        // 예약일 범위 필터
        if (searchDto.getSrchFromDate() != null && searchDto.getSrchToDate() != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getReservationDate().compareTo(searchDto.getSrchFromDate()) >= 0 &&
                            r.getReservationDate().compareTo(searchDto.getSrchToDate()) <= 0)
                    .toList();
        }

        return reservations;
    }
}
