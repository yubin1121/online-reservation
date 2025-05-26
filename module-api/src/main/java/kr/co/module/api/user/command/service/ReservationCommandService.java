package kr.co.module.api.user.command.service;
import kr.co.module.api.user.command.dto.ReservationRequestDto;
import kr.co.module.api.user.command.dto.ReservationUpdateDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.status.ReservationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationCommandService {
    private final List<ReservationDto> reservationList = new ArrayList<>();
    private final List<ProductDto> productList; // 생성자 주입 (실제 환경에서는 Repository 사용)
    private final AtomicLong reservationIdGen = new AtomicLong(1);

    public ReservationCommandService(List<ProductDto> productList) {
        this.productList = productList;
    }

    // 예약 신청
    public boolean reserve(ReservationRequestDto request) {
        // 상품 존재/예약 가능 여부 체크
        Optional<ProductDto> productOpt = productList.stream()
                .filter(p -> p.getProductId().equals(request.getProductId()))
                .filter(p -> !"Y".equals(p.getDltYsno()))
                .filter(p -> p.getTotalQuantity() != null && p.getTotalQuantity() >= request.getReservationCnt())
                .findFirst();

        if (productOpt.isEmpty()) return false;

        // 예약 정보 저장
        ReservationDto reservation = new ReservationDto();
        reservation.setReservationId(reservationIdGen.getAndIncrement());
        reservation.setProductId(request.getProductId());
        reservation.setUserId(request.getUserId());
        reservation.setReservationDate(request.getReservationDate());
        reservation.setReservationTime(request.getReservationTime());
        reservation.setReservationCnt(request.getReservationCnt());
        reservation.setReservationStatus("RESERVED");
        reservation.setCrtrId(request.getUserId());
        reservation.setCretDttm(LocalDateTime.now());
        reservation.setAmnrId(request.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());
        reservation.setDltYsno("N");
        reservationList.add(reservation);

        // 상품 수량 차감
        productOpt.get().setTotalQuantity(productOpt.get().getTotalQuantity() - request.getReservationCnt());

        return true;
    }

    // 예약 변경 (수정)
    public boolean updateReservation(ReservationUpdateDto updateDto) {
        Optional<ReservationDto> reservationOpt = reservationList.stream()
                .filter(r -> r.getReservationId().equals(updateDto.getReservationId()))
                .filter(r -> r.getUserId().equals(updateDto.getUserId()))
                .findFirst();

        if (reservationOpt.isEmpty()) return false;

        ReservationDto reservation = reservationOpt.get();

        // 상태 체크: PENDING, CONFIRMED만 변경 가능
        ReservationStatus status;
        try {
            status = ReservationStatus.valueOf(reservation.getReservationStatus());
        } catch (Exception e) {
            return false; // 잘못된 상태값이면 변경 불가
        }
        if (!(status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED)) {
            return false; // 변경 불가능한 상태
        }

        // 예약 수량 변경 시, 기존 수량 복구 + 신규 수량 차감
        int oldCnt = reservation.getReservationCnt();
        int newCnt = updateDto.getReservationCnt() != null ? updateDto.getReservationCnt() : oldCnt;

        if (updateDto.getReservationCnt() != null && newCnt != oldCnt) {
            Optional<ProductDto> productOpt = productList.stream()
                    .filter(p -> p.getProductId().equals(reservation.getProductId()))
                    .findFirst();
            if (productOpt.isPresent()) {
                int diff = newCnt - oldCnt;
                int currentQty = productOpt.get().getTotalQuantity();
                if (diff > 0 && currentQty < diff) return false; // 추가 수량 부족
                productOpt.get().setTotalQuantity(currentQty - diff);
            }
            reservation.setReservationCnt(newCnt);
        }

        if (updateDto.getReservationDate() != null) reservation.setReservationDate(updateDto.getReservationDate());
        if (updateDto.getReservationTime() != null) reservation.setReservationTime(updateDto.getReservationTime());
        if (updateDto.getReservationStatus() != null) reservation.setReservationStatus(updateDto.getReservationStatus());
        reservation.setAmnrId(updateDto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());

        return true;
    }

    // 예약 취소
    public boolean cancelReservation(ReservationUpdateDto cancelDto) {
        Optional<ReservationDto> reservationOpt = reservationList.stream()
                .filter(r -> r.getReservationId().equals(cancelDto.getReservationId()))
                .filter(r -> r.getUserId().equals(cancelDto.getUserId()))
                .findFirst();

        if (reservationOpt.isEmpty()) return false;

        // 상태 체크: PENDING, CONFIRMED만 변경 가능
        ReservationStatus status;
        try {
            status = ReservationStatus.valueOf(reservationOpt.get().getReservationStatus());
        } catch (Exception e) {
            return false; // 잘못된 상태값이면 변경 불가
        }
        if (!(status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED)) {
            return false; // 변경 불가능한 상태
        }

        ReservationDto reservation = reservationOpt.get();
        reservation.setReservationStatus("CANCELLED");
        reservation.setAmnrId(cancelDto.getUserId());
        reservation.setAmndDttm(LocalDateTime.now());

        // 상품 수량 복구
        Optional<ProductDto> productOpt = productList.stream()
                .filter(p -> p.getProductId().equals(reservation.getProductId()))
                .findFirst();
        productOpt.ifPresent(product -> product.setTotalQuantity(product.getTotalQuantity() + reservation.getReservationCnt()));

        return true;
    }
}
