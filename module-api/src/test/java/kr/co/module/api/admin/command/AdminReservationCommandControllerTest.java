package kr.co.module.api.admin.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.command.controller.AdminReservationCommandController;
import kr.co.module.api.admin.command.dto.AdminReservationUpdateDto;
import kr.co.module.api.admin.command.service.AdminReservationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReservationCommandController.class)
public class AdminReservationCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminReservationCommandService adminReservationCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("예약 상태 변경 - COMPLETED 성공")
    void updateReservationStatus_completed_success() throws Exception {
        given(adminReservationCommandService.updateReservationStatus(any(AdminReservationUpdateDto.class))).willReturn(true);

        AdminReservationUpdateDto dto = new AdminReservationUpdateDto();
        dto.setReservationId(10L);
        dto.setReservationStatus("COMPLETED");
        dto.setAdminId("adminA");

        mockMvc.perform(put("/admin/reservation/command/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("예약 상태 변경 성공"));
    }

    @Test
    @DisplayName("예약 상태 변경 - CONFIRMED 성공")
    void updateReservationStatus_confirmed_success() throws Exception {
        given(adminReservationCommandService.updateReservationStatus(any(AdminReservationUpdateDto.class))).willReturn(true);

        AdminReservationUpdateDto dto = new AdminReservationUpdateDto();
        dto.setReservationId(11L);
        dto.setReservationStatus("CONFIRMED");
        dto.setAdminId("adminA");

        mockMvc.perform(put("/admin/reservation/command/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("예약 상태 변경 성공"));
    }

    @Test
    @DisplayName("예약 상태 변경 - REJECTED 성공")
    void updateReservationStatus_rejected_success() throws Exception {
        given(adminReservationCommandService.updateReservationStatus(any(AdminReservationUpdateDto.class))).willReturn(true);

        AdminReservationUpdateDto dto = new AdminReservationUpdateDto();
        dto.setReservationId(12L);
        dto.setReservationStatus("REJECTED");
        dto.setAdminId("adminA");

        mockMvc.perform(put("/admin/reservation/command/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("예약 상태 변경 성공"));
    }

    @Test
    @DisplayName("예약 상태 변경 실패")
    void updateReservationStatus_fail() throws Exception {
        given(adminReservationCommandService.updateReservationStatus(any(AdminReservationUpdateDto.class))).willReturn(false);

        AdminReservationUpdateDto dto = new AdminReservationUpdateDto();
        dto.setReservationId(99L);
        dto.setReservationStatus("CONFIRMED");
        dto.setAdminId("adminA");

        mockMvc.perform(put("/admin/reservation/command/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").exists());
    }
}
