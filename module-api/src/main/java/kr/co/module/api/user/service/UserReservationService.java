package kr.co.module.api.user.service;
import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.dto.ReservationSearchDto;
import kr.co.module.api.user.dto.ReservationUpdateDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.status.ReservationStatus;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.UserReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserReservationService {
    private static final Logger logger = LoggerFactory.getLogger(UserReservationService.class);

    private final AdminProductRepository productRepository;
    private final UserReservationRepository reservationRepository;
    private final MongoTemplate mongoTemplate;

    public UserReservationService(AdminProductRepository productRepository,
                                  UserReservationRepository reservationRepository,
                                  MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // 예약 신청
    public ReservationDto reserve(ReservationRequestDto request) {
        Optional<ProductDto> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) return null;
        ProductDto product = productOpt.get();

        // 예약 가능 여부 체크
        if ("Y".equals(product.getDltYsno())) return null;
        if (product.getTotalQuantity() == null || product.getTotalQuantity() < request.getReservationCnt()) return null;

        // 예약 정보 저장
        ReservationDto reservation = ReservationDto.builder()
                //.reservationId(자동생성)
                .productId(request.getProductId())
                .userId(request.getUserId())
                .reservationDate(request.getReservationDate())
                .reservationTime(request.getReservationTime())
                .reservationCnt(request.getReservationCnt())
                .reservationStatus(ReservationStatus.COMPLETED.name())
                .crtrId(request.getUserId())
                .cretDttm(LocalDateTime.now())
                .amnrId(request.getUserId())
                .amndDttm(LocalDateTime.now())
                .dltYsno("N")
                .build();

        reservation = reservationRepository.save(reservation);

        // 상품 수량 차감
        product.setTotalQuantity(product.getTotalQuantity() - request.getReservationCnt());
        productRepository.save(product);

        return reservation;
    }

    // 예약 변경 (수정)
    public ReservationDto updateReservation(ReservationUpdateDto updateDto) {
        Optional<ReservationDto> reservationOpt = reservationRepository.findById(updateDto.getReservationId());
        if (reservationOpt.isEmpty()) return null;

        ReservationDto reservation = reservationOpt.get();

        // 본인 예약만 수정 가능
        if (!reservation.getUserId().equals(updateDto.getUserId())) return null;

        // 상태 체크
        ReservationStatus status;
        try {
            status = ReservationStatus.valueOf(reservation.getReservationStatus());
        } catch (Exception e) {
            return null;
        }
        if (!(status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED)) return null;

        // 예약 수량 변경
        int oldCnt = reservation.getReservationCnt();
        int newCnt = updateDto.getReservationCnt() != null ? updateDto.getReservationCnt() : oldCnt;

        if (updateDto.getReservationCnt() != null && newCnt != oldCnt) {
            Optional<ProductDto> productOpt = productRepository.findById(reservation.getProductId());
            if (productOpt.isPresent()) {
                ProductDto product = productOpt.get();
                int diff = newCnt - oldCnt;
                int currentQty = product.getTotalQuantity();
                if (diff > 0 && currentQty < diff) return null; // 추가 수량 부족
                product.setTotalQuantity(currentQty - diff);
                productRepository.save(product);
            }
            reservation.setReservationCnt(newCnt);
        }

        if (updateDto.getReservationDate() != null) reservation.setReservationDate(updateDto.getReservationDate());
        if (updateDto.getReservationTime() != null) reservation.setReservationTime(updateDto.getReservationTime());
        if (updateDto.getReservationStatus() != null) reservation.setReservationStatus(updateDto.getReservationStatus());
        reservation.setAmnrId(updateDto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    // 예약 취소
    public ReservationDto cancelReservation(ReservationUpdateDto cancelDto) {
        Optional<ReservationDto> reservationOpt = reservationRepository.findById(cancelDto.getReservationId());
        if (reservationOpt.isEmpty()) return null;

        ReservationDto reservation = reservationOpt.get();

        // 본인 예약만 취소 가능
        if (!reservation.getUserId().equals(cancelDto.getUserId())) return null;

        // 상태 체크
        ReservationStatus status;
        try {
            status = ReservationStatus.valueOf(reservation.getReservationStatus());
        } catch (Exception e) {
            return null;
        }
        if (!(status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED)) return null;

        reservation.setReservationStatus(ReservationStatus.CANCELED.name());
        reservation.setAmnrId(cancelDto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());

        // 상품 수량 복구
        Optional<ProductDto> productOpt = productRepository.findById(reservation.getProductId());
        productOpt.ifPresent(product -> {
            product.setTotalQuantity(product.getTotalQuantity() + reservation.getReservationCnt());
            productRepository.save(product);
        });

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
