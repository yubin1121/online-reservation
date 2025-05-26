package kr.co.module.core.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    // 예약 아이디(상품ID+예약자ID+일자+시간)
    private Long reservationId;
    // 상품 아이디
    private Long productId;
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

    // 생성자 ID
    private String crtrId;
    // 생성일시
    private LocalDateTime cretDttm;
    // 수정자 ID
    private String amnrId;
    // 수정일시
    private LocalDateTime amndDttm;
    // 삭제여부
    private String dltYsno;
}
