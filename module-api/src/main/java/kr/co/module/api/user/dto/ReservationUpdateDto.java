package kr.co.module.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservation")
public class ReservationUpdateDto {

    // 예약 아이디(상품ID+예약자ID+일자+시간)
    @Id
    private Long reservationId;
    // 예약자 아이디
    private String userId;
    // 예약 일자
    private String reservationDate;
    // 예약 시간
    private String reservationTime;
    // 예약 수량
    private Integer reservationCnt;
    // 예약 상태
    private String reservationStatus;
}
