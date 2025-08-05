package kr.co.module.api.user.controller;

import jakarta.validation.Valid;
import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.dto.ReservationSearchDto;
import kr.co.module.api.user.dto.ReservationUpdateDto;
import kr.co.module.api.user.service.UserReservationService;
import kr.co.module.core.domain.Reservation;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import kr.co.module.core.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/reservation/")
public class UserReservationController {
    private final UserReservationService userReservationService;

    // 예약 신청
    @PostMapping("request")
    public ResponseEntity<?> reserve(@Valid @RequestBody ReservationRequestDto requestDto) {
        Reservation result = userReservationService.reserve(requestDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 신청 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_FAIL.message(), ErrorCode.RESERVATION_FAIL.code())
            );
        }
    }

    // 예약 변경
    @PutMapping("modify")
    public ResponseEntity<?> updateReservation(@Valid @RequestBody ReservationUpdateDto updateDto) {
        Reservation result = userReservationService.updateReservation(updateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 변경 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_UPDATE_FAIL.message(), ErrorCode.RESERVATION_UPDATE_FAIL.code())
            );
        }
    }

    // 예약 취소
    @PostMapping("cancel")
    public ResponseEntity<?> cancelReservation(@Valid @RequestBody ReservationUpdateDto cancelDto) {
        Reservation result = userReservationService.cancelReservation(cancelDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 취소 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_CANCEL_FAIL.message(), ErrorCode.RESERVATION_CANCEL_FAIL.code())
            );
        }
    }

    @GetMapping("search")
    public CompletableFuture<ResponseEntity<ApiResponse<List<Reservation>>>> searchUserReservations(
            @Valid @ModelAttribute ReservationSearchDto searchDto
    ) {
        return userReservationService.searchUserReservations(searchDto)
                .thenApply(reservations ->
                        ResponseEntity.ok(
                                new ApiResponse<>(true, reservations, "예약 정보 조회 성공", null)
                        )
                )
                .exceptionally(ex -> {
                    // 오류 발생 시 동일한 제네릭 타입 사용
                    return ResponseEntity.internalServerError().body(
                            new ApiResponse<>(false, null, null,
                                    new ErrorResponse("예약 정보 조회 실패", "RESERVATION_SEARCH_ERROR"))
                    );
                });
    }
}
