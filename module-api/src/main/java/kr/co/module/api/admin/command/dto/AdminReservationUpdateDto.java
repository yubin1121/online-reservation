package kr.co.module.api.admin.command.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationUpdateDto {
    @NotNull(message = "예약ID는 필수입니다.")
    private Long reservationId;
    @NotNull(message = "변경 예정 예약 상태는 필수입니다.")
    private String reservationStatus;
    @NotNull(message = "관리자 ID는 필수입니다.")
    private String adminId;
}
