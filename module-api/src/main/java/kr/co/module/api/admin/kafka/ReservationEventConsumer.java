package kr.co.module.api.admin.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.service.AdminReservationService;
import kr.co.module.core.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationEventConsumer {

    private final ObjectMapper objectMapper;
    private final AdminReservationService adminReservationService;

    @KafkaListener(topics = "reservation-created-topic", groupId = "admin-group")
    public void consumeReservationCreated(String message) {
        try {
            Reservation reservation = objectMapper.readValue(message, Reservation.class);
            log.info("예약 생성 이벤트 수신: {}", reservation);

            // admin service 호출
            adminReservationService.handleNewReservationEvent(reservation);

        } catch (Exception e) {
            log.error("Kafka 메시지 역직렬화 실패", e);
        }
    }
}