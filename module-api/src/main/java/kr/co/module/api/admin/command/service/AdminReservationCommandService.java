package kr.co.module.api.admin.command.service;
import kr.co.module.api.admin.command.dto.AdminReservationUpdateDto;
import kr.co.module.core.dto.domain.ReservationDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminReservationCommandService {
    private final List<ReservationDto> reservationList;

    public AdminReservationCommandService(List<ReservationDto> reservationList) {
        this.reservationList = reservationList;
    }

    // 예약 상태 변경
    public boolean updateReservationStatus(AdminReservationUpdateDto updateDto) {
        Optional<ReservationDto> reservationOpt = reservationList.stream()
                .filter(r -> r.getReservationId().equals(updateDto.getReservationId()))
                .findFirst();

        if (reservationOpt.isEmpty()) return false;

        ReservationDto reservation = reservationOpt.get();
        reservation.setReservationStatus(updateDto.getReservationStatus());
        reservation.setAmnrId(updateDto.getAdminId());
        reservation.setAmndDttm(LocalDateTime.now());
        return true;
    }
}
