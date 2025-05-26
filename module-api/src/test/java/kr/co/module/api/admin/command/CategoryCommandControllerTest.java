package kr.co.module.api.admin.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.module.api.admin.command.controller.CategoryCommandController;
import kr.co.module.api.admin.command.dto.CategoryCreateDto;
import kr.co.module.api.admin.command.dto.CategoryUpdateDto;
import kr.co.module.api.admin.command.service.CategoryCommandService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryCommandController.class)
class CategoryCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryCommandService categoryCommandService;

    @Test
    @DisplayName("전시회 카테고리 등록 성공")
    void createExhibitionCategory() throws Exception {

        given(categoryCommandService.createCategory(any(CategoryCreateDto.class))).willReturn(true);

        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setCategoryName("전시회");
        dto.setCategoryDesc("미술, 박람회 등 전시 행사");
        dto.setCategoryOrder(3);
        dto.setAdminId("admin");

        mockMvc.perform(post("/admin/category/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));


        System.out.println("전시회 카테고리 등록 성공 테스트 통과!");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_success() throws Exception {
        // given
        given(categoryCommandService.updateCategory(any(CategoryUpdateDto.class))).willReturn(true);

        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId(1L);
        updateDto.setCategoryDesc("수정된 설명");
        updateDto.setCategoryOrder(10);
        updateDto.setAdminId("admin");

        mockMvc.perform(put("/admin/category/command/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 수정 성공"));
    }

    @Test
    @DisplayName("카테고리 수정 실패")
    void updateCategory_fail() throws Exception {
        // given
        given(categoryCommandService.updateCategory(any(CategoryUpdateDto.class))).willReturn(false);

        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId(null);
        updateDto.setAdminId("admin");

        mockMvc.perform(put("/admin/category/command/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_success() throws Exception {
        // given
        given(categoryCommandService.deleteCategory(any(CategoryUpdateDto.class))).willReturn(true);

        CategoryUpdateDto deleteDto = new CategoryUpdateDto();
        deleteDto.setCategoryId(1L);
        deleteDto.setAdminId("admin");

        mockMvc.perform(delete("/admin/category/command/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 삭제 성공"));
    }

    @Test
    @DisplayName("카테고리 삭제 실패")
    void deleteCategory_fail() throws Exception {
        // given
        given(categoryCommandService.deleteCategory(any(CategoryUpdateDto.class))).willReturn(false);

        CategoryUpdateDto deleteDto = new CategoryUpdateDto();
        deleteDto.setCategoryId(null);
        deleteDto.setAdminId("admin");

        mockMvc.perform(delete("/admin/category/command/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }
}
