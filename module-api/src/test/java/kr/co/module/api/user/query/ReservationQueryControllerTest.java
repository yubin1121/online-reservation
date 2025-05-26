package kr.co.module.api.user.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.user.query.controller.ReservationQueryController;
import kr.co.module.api.user.query.dto.ReservationSearchDto;
import kr.co.module.api.user.query.service.ReservationQueryService;
import kr.co.module.core.dto.domain.ReservationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationQueryController.class)
class ReservationQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationQueryService reservationQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자 예약 정보 조회 성공")
    void searchUserReservations_success() throws Exception {

        ReservationDto reservation = new ReservationDto();
        reservation.setReservationId(1L);
        reservation.setProductId(2L);
        reservation.setUserId("10001");
        reservation.setReservationDate("20250610");
        reservation.setReservationTime("1400");
        reservation.setReservationCnt(2);
        reservation.setReservationStatus("CONFIRMED");

        given(reservationQueryService.searchUserReservations(any(ReservationSearchDto.class)))
                .willReturn(List.of(reservation));

        // when & then
        mockMvc.perform(get("/user/reservation/query")
                        .param("userId", "10001")
                        .param("srchFromDate", "20250601")
                        .param("srchToDate", "20250630")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("10001"))
                .andExpect(jsonPath("$.data[0].reservationDate").value("20250610"));
    }

    @Test
    @DisplayName("필수 파라미터 누락 시 400 반환")
    void searchUserReservations_fail_required() throws Exception {
        // when & then
        mockMvc.perform(get("/user/reservation/query")
                        .param("userId", "11111")
                        .param("srchToDate", "20250630")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}