package kr.co.module.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationSearchDto {

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;

    private String productId;

    private String categoryId;

    private String reservationStatus;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}