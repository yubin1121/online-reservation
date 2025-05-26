package kr.co.module.api.admin.query.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.query.dto.CategorySearchDto;
import kr.co.module.api.admin.query.service.CategoryQueryService;
import kr.co.module.core.dto.domain.CategoryDto;
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

@WebMvcTest(CategoryQueryController.class)
class CategoryQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryQueryService categoryQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("카테고리 조건 검색 성공")
    void searchCategories_success() throws Exception {
        CategoryDto dto1 = new CategoryDto();
        dto1.setCategoryId(1L);
        dto1.setCategoryName("전시회");
        dto1.setCategoryDesc("미술, 박람회 등 전시 행사");
        dto1.setCategoryOrder(3);
        dto1.setCrtrId("admin");

        List<CategoryDto> mockList = List.of(dto1);

        given(categoryQueryService.searchCategories(any(CategorySearchDto.class)))
                .willReturn(mockList);

        mockMvc.perform(get("/admin/category/query/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryName").value("전시회"));
    }

    @Test
    @DisplayName("카테고리명 검색")
    void searchCategories_byName() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(2L);
        dto.setCategoryName("음식점");
        dto.setCategoryDesc("맛집, 레스토랑 등");
        dto.setCategoryOrder(2);
        dto.setCrtrId("admin");

        given(categoryQueryService.searchCategories(any(CategorySearchDto.class)))
                .willReturn(List.of(dto));

        mockMvc.perform(get("/admin/category/query/search")
                        .param("categoryName", "음식점")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryName").value("음식점"));
    }
}
