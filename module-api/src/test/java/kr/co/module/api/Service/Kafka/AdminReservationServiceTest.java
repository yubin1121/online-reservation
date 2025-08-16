package kr.co.module.api.Service.Kafka;

import kr.co.module.api.admin.service.AdminReservationService;
import kr.co.module.core.domain.Reservation;
import kr.co.module.mapper.repository.AdminReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminReservationServiceTest {

    @Mock
    private AdminReservationRepository adminReservationRepository;

    @InjectMocks
    private AdminReservationService adminReservationService;

    private Reservation existingReservation;
    private Reservation incomingReservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingReservation = Reservation.builder()
                .id("existing-id")
                .reservationBizId("biz-123")
                .reservationStatus("PENDING")
                .amnrId("admin1")
                .amndDttm(LocalDateTime.now().minusDays(1))
                .build();

        incomingReservation = Reservation.builder()
                .id("incoming-id")
                .reservationBizId("biz-123")
                .reservationStatus("CONFIRMED")
                .amnrId("admin2")
                .amndDttm(LocalDateTime.now())
                .build();
    }

    @Test
    void testHandleNewReservationEvent_UpdateExistingReservation() {
        when(adminReservationRepository.findByReservationBizId("biz-123"))
                .thenReturn(Optional.of(existingReservation));
        when(adminReservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        adminReservationService.handleNewReservationEvent(incomingReservation);

        // 기존 예약 상태가 수정되었는지 검증
        verify(adminReservationRepository).save(argThat(reservation ->
                reservation.getReservationStatus().equals("CONFIRMED") &&
                        reservation.getAmnrId().equals("admin2")
        ));
    }

    @Test
    void testHandleNewReservationEvent_SaveNewReservation() {
        when(adminReservationRepository.findByReservationBizId("biz-123"))
                .thenReturn(Optional.empty());
        when(adminReservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        adminReservationService.handleNewReservationEvent(incomingReservation);

        // 신규 예약 저장 호출 검증
        verify(adminReservationRepository).save(incomingReservation);
    }

    @Test
    void testHandleNewReservationEvent_NoStatusChange() {
        Reservation noChangeReservation = Reservation.builder()
                .id("incoming-id")
                .reservationBizId("biz-123")
                .reservationStatus("PENDING") // 기존 상태와 동일
                .amnrId("admin2")
                .amndDttm(LocalDateTime.now())
                .build();

        when(adminReservationRepository.findByReservationBizId("biz-123"))
                .thenReturn(Optional.of(existingReservation));
        when(adminReservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        adminReservationService.handleNewReservationEvent(noChangeReservation);

        // 상태 변화가 없으면 save 호출이 없어야 하지만 설계에 따라 호출 여부 다를 수 있다면 아래 검증은 생략해주세요
        verify(adminReservationRepository, never()).save(any(Reservation.class));
    }
}
