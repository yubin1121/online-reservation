package kr.co.module.api.admin.query.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationSearchDto {

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;

    private long productId;

    private long categoryId;
}
