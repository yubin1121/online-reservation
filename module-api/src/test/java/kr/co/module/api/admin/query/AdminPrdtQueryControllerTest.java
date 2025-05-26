package kr.co.module.api.admin.query;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.query.controller.AdminProductQueryController;
import kr.co.module.api.admin.query.dto.AdminProductSearchDto;
import kr.co.module.api.admin.query.service.AdminProductQueryService;
import kr.co.module.core.dto.domain.ProductDto;
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

@WebMvcTest(AdminProductQueryController.class)
public class AdminPrdtQueryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminProductQueryService adminProductQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("본인 상품만 조회 성공")
    void searchMyProducts_success() throws Exception {
        ProductDto product = new ProductDto();
        product.setProductId(1L);
        product.setProductName("hong");
        product.setCrtrId("11111");

        given(adminProductQueryService.searchMyProducts(any(AdminProductSearchDto.class)))
                .willReturn(List.of(product));

        mockMvc.perform(get("/admin/product/query/my")
                        .param("adminId", "admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productName").value("hong"))
                .andExpect(jsonPath("$.data[0].crtrId").value("11111"));
    }
}
