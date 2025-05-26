package kr.co.module.api.user.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.user.command.controller.ReservationCommandController;
import kr.co.module.api.user.command.dto.ReservationRequestDto;
import kr.co.module.api.user.command.dto.ReservationUpdateDto;
import kr.co.module.api.user.command.service.ReservationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationCommandController.class)
class ReservationCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationCommandService reservationCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("예약 신청 성공")
    void reserve_success() throws Exception {
        given(reservationCommandService.reserve(any(ReservationRequestDto.class))).willReturn(true);

        ReservationRequestDto dto = new ReservationRequestDto();
        dto.setProductId(1L);
        dto.setUserId("userA");
        dto.setReservationDate("20250601");
        dto.setReservationTime("1400");
        dto.setReservationCnt(2);

        mockMvc.perform(post("/user/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("예약 신청 성공"));
    }

    @Test
    @DisplayName("예약 신청 실패")
    void reserve_fail() throws Exception {
        given(reservationCommandService.reserve(any(ReservationRequestDto.class))).willReturn(false);

        ReservationRequestDto dto = new ReservationRequestDto();
        dto.setProductId(1L);
        dto.setUserId("userA");
        dto.setReservationDate("20250601");
        dto.setReservationTime("1400");
        dto.setReservationCnt(2);

        mockMvc.perform(post("/user/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("예약 변경 성공")
    void updateReservation_success() throws Exception {
        given(reservationCommandService.updateReservation(any(ReservationUpdateDto.class))).willReturn(true);

        ReservationUpdateDto dto = new ReservationUpdateDto();
        dto.setReservationId(10L);
        dto.setUserId("userA");
        dto.setReservationDate("20250602");
        dto.setReservationTime("1500");
        dto.setReservationCnt(3);
        dto.setReservationStatus("CONFIRMED");

        mockMvc.perform(put("/user/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("예약 변경 성공"));
    }

    @Test
    @DisplayName("예약 변경 실패")
    void updateReservation_fail() throws Exception {
        given(reservationCommandService.updateReservation(any(ReservationUpdateDto.class))).willReturn(false);

        ReservationUpdateDto dto = new ReservationUpdateDto();
        dto.setReservationId(10L);
        dto.setUserId("userA");

        mockMvc.perform(put("/user/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_success() throws Exception {
        given(reservationCommandService.cancelReservation(any(ReservationUpdateDto.class))).willReturn(true);

        ReservationUpdateDto dto = new ReservationUpdateDto();
        dto.setReservationId(10L);
        dto.setUserId("userA");

        mockMvc.perform(post("/user/reservation/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("예약 취소 성공"));
    }

    @Test
    @DisplayName("예약 취소 실패")
    void cancelReservation_fail() throws Exception {
        given(reservationCommandService.cancelReservation(any(ReservationUpdateDto.class))).willReturn(false);

        ReservationUpdateDto dto = new ReservationUpdateDto();
        dto.setReservationId(10L);
        dto.setUserId("userA");

        mockMvc.perform(post("/user/reservation/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").exists());
    }
}
