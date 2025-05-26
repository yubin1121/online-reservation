package kr.co.module.api.admin.command.controller;

import jakarta.validation.Valid;
import kr.co.module.api.admin.command.dto.AdminReservationUpdateDto;
import kr.co.module.api.admin.command.service.AdminReservationCommandService;
import kr.co.module.core.response.ApiResponse;
import kr.co.module.core.response.ErrorResponse;
import kr.co.module.core.code.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reservation/command")
public class AdminReservationCommandController {

    private final AdminReservationCommandService adminReservationCommandService;

    public AdminReservationCommandController(AdminReservationCommandService adminReservationCommandService) {
        this.adminReservationCommandService = adminReservationCommandService;
    }

    // 예약 상태 변경 (완료, 확정, 거절)
    @PutMapping("/status")
    public ResponseEntity<?> updateReservationStatus(@Valid @RequestBody AdminReservationUpdateDto updateDto) {
        boolean result = adminReservationCommandService.updateReservationStatus(updateDto);
        if (result) {
            return ResponseEntity.ok(new ApiResponse<>(true, null, "예약 상태 변경 성공", null));
        } else {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(ErrorCode.RESERVATION_UPDATE_FAIL.message(), ErrorCode.RESERVATION_UPDATE_FAIL.code(), null)
            );
        }
    }
}
