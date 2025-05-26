package kr.co.module.api.admin.query.service;

import kr.co.module.api.admin.query.dto.CategorySearchDto;
import kr.co.module.core.dto.domain.CategoryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CategoryQueryServiceTest {

    @Test
    @DisplayName("카테고리명, 정렬순서로 검색")
    void searchCategories_byNameAndOrder() {

        List<CategoryDto> categoryList = new ArrayList<>();
        categoryList.add(new CategoryDto(1L, "전시회", "미술, 박람회", 3, "admin", LocalDateTime.now(), "admin", LocalDateTime.now(), "N"));
        categoryList.add(new  CategoryDto(2L, "음식점", "맛집", 2, "admin", LocalDateTime.now(), "admin", LocalDateTime.now(), "N"));
        categoryList.add(new CategoryDto(3L, "전시회", "기타 전시", 1, "admin", LocalDateTime.now(), "admin", LocalDateTime.now(), "N"));

        CategoryQueryService categoryQueryService = new CategoryQueryService(categoryList);

        CategorySearchDto searchDto = new CategorySearchDto();
        //searchDto.setCategoryName("전시회");
        //searchDto.setCategoryOrder(3);
        //searchDto.setAdminId("admin");

        List<CategoryDto> result = categoryQueryService.searchCategories(searchDto);

        System.out.println("조회 결과: " + result);
    }
}
