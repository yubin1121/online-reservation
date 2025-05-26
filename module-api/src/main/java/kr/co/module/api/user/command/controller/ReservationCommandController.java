package kr.co.module.api.user.command.controller;

import jakarta.validation.Valid;
import kr.co.module.api.user.command.dto.ReservationRequestDto;
import kr.co.module.api.user.command.dto.ReservationUpdateDto;
import kr.co.module.api.user.command.service.ReservationCommandService;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import kr.co.module.core.code.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/reservation")
public class ReservationCommandController {
    private final ReservationCommandService reservationCommandService;

    public ReservationCommandController(ReservationCommandService reservationCommandService) {
        this.reservationCommandService = reservationCommandService;
    }

    // 예약 신청
    @PostMapping
    public ResponseEntity<?> reserve(@Valid @RequestBody ReservationRequestDto requestDto) {
        boolean result = reservationCommandService.reserve(requestDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 신청 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_FAIL.message(), ErrorCode.RESERVATION_FAIL.code(), null)
            );
        }
    }

    // 예약 변경
    @PutMapping
    public ResponseEntity<?> updateReservation(@Valid @RequestBody ReservationUpdateDto updateDto) {
        boolean result = reservationCommandService.updateReservation(updateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 변경 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_UPDATE_FAIL.message(), ErrorCode.RESERVATION_UPDATE_FAIL.code(), null)
            );
        }
    }

    // 예약 취소
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelReservation(@Valid @RequestBody ReservationUpdateDto cancelDto) {
        boolean result = reservationCommandService.cancelReservation(cancelDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 취소 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_CANCEL_FAIL.message(), ErrorCode.RESERVATION_CANCEL_FAIL.code(), null)
            );
        }
    }
}
