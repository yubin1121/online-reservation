package kr.co.module.api.user.query.controller;

import jakarta.validation.Valid;
import kr.co.module.api.user.query.dto.ReservationSearchDto;
import kr.co.module.api.user.query.service.ReservationQueryService;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/reservation/query")
public class ReservationQueryController {

    private final ReservationQueryService reservationQueryService;

    public ReservationQueryController(ReservationQueryService reservationQueryService) {
        this.reservationQueryService = reservationQueryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationDto>>> searchUserReservations(
            @Valid @ModelAttribute ReservationSearchDto searchDto
    ) {
        List<ReservationDto> result = reservationQueryService.searchUserReservations(searchDto);
        return ResponseEntity.ok(new ApiResponse<>(true, result, "예약 정보 조회 성공", null));
    }
}