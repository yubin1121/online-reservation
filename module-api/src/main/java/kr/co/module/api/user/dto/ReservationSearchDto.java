package kr.co.module.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSearchDto {

    //예약자Id
    @NotBlank(message = "사용자정보는 필수입니다.")
    private String userId;

    //카테고리 아이디
    private String categoryId;

    @NotBlank(message = "조회일자는 필수입니다.")
    private String srchFromDate;

    @NotBlank(message = "조회일자는 필수입니다.")
    private String srchToDate;
}


