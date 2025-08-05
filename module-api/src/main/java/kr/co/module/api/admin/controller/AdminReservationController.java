package kr.co.module.api.admin.controller;

import jakarta.validation.Valid;
import kr.co.module.api.admin.dto.*;
import kr.co.module.api.admin.service.AdminReservationService;
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
@RequestMapping("/admin/reservation/")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    // 예약 상태 변경 (완료, 확정, 거절)
    @PutMapping("status")
    public ResponseEntity<?> updateReservationStatus(@Valid @RequestBody AdminReservationUpdateDto updateDto) {
        Reservation result = adminReservationService.updateReservationStatus(updateDto);
        if (result != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 상태 변경 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_UPDATE_FAIL.message(), ErrorCode.RESERVATION_UPDATE_FAIL.code())
            );
        }
    }

    @GetMapping("search")
    public CompletableFuture<ResponseEntity<ApiResponse<List<Reservation>>>> searchAdminReservations(
            @Valid @ModelAttribute AdminReservationSearchDto searchDto
    ) {
        return adminReservationService.searchAdminReservations(searchDto)
                .thenApply(reservations ->
                        ResponseEntity.ok(
                                new ApiResponse<>(true, reservations, "예약 현황 조회 성공", null)
                        )
                )
                .exceptionally(ex -> {
                    return ResponseEntity.internalServerError().body(
                            new ApiResponse<>(false, null, null,
                                    new ErrorResponse("예약 현황 조회 실패", "ADMIN_RESERVATION_SEARCH_ERROR"))
                    );
                });
    }
}
