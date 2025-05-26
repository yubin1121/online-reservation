package kr.co.module.api.admin.query;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.query.controller.AdminReservationQueryController;
import kr.co.module.api.admin.query.dto.AdminReservationSearchDto;
import kr.co.module.api.admin.query.service.AdminReservationQueryService;
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

@WebMvcTest(AdminReservationQueryController.class)
public class AdminReservationQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminReservationQueryService adminReservationQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("관리자 예약 현황 조회 성공")
    void searchAdminReservations_success() throws Exception {
        // given
        ReservationDto reservation = new ReservationDto();
        reservation.setReservationId(1L);
        reservation.setProductId(123L);
        reservation.setUserId("userA");
        reservation.setReservationDate("2025-06-10");
        reservation.setReservationTime("14:00");
        reservation.setReservationCnt(2);
        reservation.setReservationStatus("CONFIRMED");

        given(adminReservationQueryService.searchAdminReservations(any(AdminReservationSearchDto.class)))
                .willReturn(List.of(reservation));

        // when & then
        mockMvc.perform(get("/admin/reservation/query")
                        .param("adminId", "adminA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reservationId").value(1))
                .andExpect(jsonPath("$.data[0].productId").value(123))
                .andExpect(jsonPath("$.data[0].userId").value("userA"))
                .andExpect(jsonPath("$.data[0].reservationStatus").value("CONFIRMED"));
    }

    @Test
    @DisplayName("관리자 예약 현황 조회 실패")
    void searchAdminReservations_fai() throws Exception {
        // adminId 누락
        mockMvc.perform(get("/admin/reservation/query")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
