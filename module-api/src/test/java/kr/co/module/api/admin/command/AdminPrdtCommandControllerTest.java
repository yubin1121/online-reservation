package kr.co.module.api.admin.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.command.controller.ProductCommandController;
import kr.co.module.api.admin.command.dto.ProductCreateDto;
import kr.co.module.api.admin.command.dto.ProductUpdateDto;
import kr.co.module.api.admin.command.service.ProductCommandService;
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

@WebMvcTest(ProductCommandController.class)
public class AdminPrdtCommandControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCommandService productCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 등록 성공")
    void createProduct_success() throws Exception {
        given(productCommandService.createProduct(any(ProductCreateDto.class))).willReturn(true);

        ProductCreateDto dto = new ProductCreateDto();
        dto.setCategoryId(1L);
        dto.setProductName("hong");
        dto.setProductDesc("네일샵");
        dto.setProductPlace("강남점");
        dto.setProductLocation("서울 강남구");
        dto.setAdminId("11111");

        mockMvc.perform(post("/admin/product/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 등록 성공"));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_success() throws Exception {
        given(productCommandService.updateProduct(any(ProductUpdateDto.class))).willReturn(true);

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setProductId(1L);
        dto.setProductName("hoong");
        dto.setAdminId("11111");

        mockMvc.perform(put("/admin/product/command/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 수정 성공"));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_success() throws Exception {
        given(productCommandService.deleteProduct(any(ProductUpdateDto.class))).willReturn(true);

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setProductId(1L);
        dto.setAdminId("11111");

        mockMvc.perform(delete("/admin/product/command/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 삭제 성공"));
    }
}

