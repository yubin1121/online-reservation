package kr.co.module.api.user.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.user.query.controller.ProductQueryController;
import kr.co.module.api.user.query.dto.ProductSearchDto;
import kr.co.module.api.user.query.service.ProductQueryService;
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

@WebMvcTest(ProductQueryController.class)
public class ProductQueryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductQueryService productQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 검색 성공")
    void searchProductsByCategoryId_success() throws Exception {

        ProductDto product = new ProductDto();
        product.setProductId(1L);
        product.setProductName("네일샵");
        product.setCategoryId(2L);
        product.setProductPlace("강남");
        product.setCrtrId("11111");

        given(productQueryService.searchProducts(any(ProductSearchDto.class)))
                .willReturn(List.of(product));

        mockMvc.perform(get("/user/product/query/search")
                        .param("categoryId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productName").value("네일샵"))
                .andExpect(jsonPath("$.data[0].categoryId").value(2));
    }

    @Test
    @DisplayName("상품 검색 실패")
    void searchProducts_fail_requiredCondition() throws Exception {
        mockMvc.perform(get("/user/product/query/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").value(""))
                .andExpect(jsonPath("$.details").isEmpty());
    }

}
