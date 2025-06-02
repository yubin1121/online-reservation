package kr.co.module.api.user.controller;

import jakarta.validation.Valid;
import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.dto.ReservationSearchDto;
import kr.co.module.api.user.dto.ReservationUpdateDto;
import kr.co.module.api.user.service.UserReservationService;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import kr.co.module.core.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/reservation/")
public class UserReservationController {
    private final UserReservationService userReservationService;

    // 예약 신청
    @PostMapping("request")
    public ResponseEntity<?> reserve(@Valid @RequestBody ReservationRequestDto requestDto) {
        ReservationDto result = userReservationService.reserve(requestDto);
        if (result == null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 신청 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_FAIL.message(), ErrorCode.RESERVATION_FAIL.code(), null)
            );
        }
    }

    // 예약 변경
    @PutMapping("modify")
    public ResponseEntity<?> updateReservation(@Valid @RequestBody ReservationUpdateDto updateDto) {
        ReservationDto result = userReservationService.updateReservation(updateDto);
        if (result == null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 변경 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_UPDATE_FAIL.message(), ErrorCode.RESERVATION_UPDATE_FAIL.code(), null)
            );
        }
    }

    // 예약 취소
    @PostMapping("cancel")
    public ResponseEntity<?> cancelReservation(@Valid @RequestBody ReservationUpdateDto cancelDto) {
        ReservationDto result = userReservationService.cancelReservation(cancelDto);
        if (result == null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 취소 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_CANCEL_FAIL.message(), ErrorCode.RESERVATION_CANCEL_FAIL.code(), null)
            );
        }
    }

    @GetMapping("search")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> searchUserReservations(
            @Valid @ModelAttribute ReservationSearchDto searchDto
    ) {
        List<ReservationDto> result = userReservationService.searchUserReservations(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "예약 정보 조회 성공", null));
    }
}
