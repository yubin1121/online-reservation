package kr.co.module.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservation")
public class ReservationRequestDto {

    // 상품 아이디
    @Id
    private Long productId;
    // 예약자 아이디
    private String userId;
    // 예약 일자
    private String reservationDate;
    // 예약 시간
    private String reservationTime;
    // 예약 수량
    private Integer reservationCnt;
}
