package kr.co.module.api.admin.query.controller;
import jakarta.validation.Valid;
import kr.co.module.api.admin.query.dto.AdminReservationSearchDto;
import kr.co.module.api.admin.query.service.AdminReservationQueryService;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/reservation/query")
public class AdminReservationQueryController {

    private final AdminReservationQueryService adminReservationQueryService;

    public AdminReservationQueryController(AdminReservationQueryService adminReservationQueryService) {
        this.adminReservationQueryService = adminReservationQueryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationDto>>> searchAdminReservations(
            @Valid @ModelAttribute AdminReservationSearchDto searchDto
    ) {
        List<ReservationDto> result = adminReservationQueryService.searchAdminReservations(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "예약 현황 조회 성공", null));
    }
}
