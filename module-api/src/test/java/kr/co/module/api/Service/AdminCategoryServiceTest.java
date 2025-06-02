package kr.co.module.api.Service;

import kr.co.module.api.admin.dto.*;
import kr.co.module.api.admin.service.AdminCategoryService;
import kr.co.module.core.dto.domain.CategoryDto;
import kr.co.module.mapper.repository.AdminCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminCategoryServiceTest {

    private MongoTemplate mongoTemplate;

    private AdminCategoryService adminCategoryService;

    AdminCategoryRepository adminCategoryRepository = mock(AdminCategoryRepository.class);

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        adminCategoryService = new AdminCategoryService(mongoTemplate, adminCategoryRepository);
    }
    @Test
    void createCategory_정상생성() {
        // given
        CategoryCreateDto createDto = new CategoryCreateDto();
        createDto.setCategoryName("카테고리1");
        createDto.setCategoryDesc("설명");
        createDto.setCategoryOrder(1);
        createDto.setAdminId("admin1");

        ArgumentCaptor<CategoryDto> captor = ArgumentCaptor.forClass(CategoryDto.class);

        // when
        CategoryDto result = adminCategoryService.createCategory(createDto);

        // then
        verify(adminCategoryRepository, times(1)).save(captor.capture());
        CategoryDto saved = captor.getValue();
        assertEquals("카테고리1", saved.getCategoryName());
        assertEquals("설명", saved.getCategoryDesc());
        assertEquals(1, saved.getCategoryOrder());
        assertEquals("admin1", saved.getCrtrId());
        assertEquals("N", saved.getDltYsno());
        assertNotNull(result);
    }

    @Test
    void updateCategory_정상수정() {
        // given
        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId(100L);
        updateDto.setCategoryDesc("수정된 설명");
        updateDto.setCategoryOrder(2);
        updateDto.setAdminId("admin2");

        CategoryDto origin = CategoryDto.builder()
                .categoryId(100L)
                .categoryName("카테고리1")
                .categoryDesc("기존 설명")
                .categoryOrder(1)
                .crtrId("admin2")
                .dltYsno("N")
                .build();

        when(adminCategoryRepository.findById(100L)).thenReturn(Optional.of(origin));
        when(adminCategoryRepository.save(any(CategoryDto.class))).thenReturn(origin);

        // when
        CategoryDto result = adminCategoryService.updateCategory(updateDto);

        // then
        assertNotNull(result);
        assertEquals("수정된 설명", result.getCategoryDesc());
        assertEquals(2, result.getCategoryOrder());
        assertEquals("admin2", result.getAmnrId());
        assertNotNull(result.getAmndDttm());
        verify(adminCategoryRepository).save(origin);
    }

    @Test
    void updateCategory_없는카테고리() {
        // given
        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId(999L);
        when(adminCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        CategoryDto result = adminCategoryService.updateCategory(updateDto);

        // then
        assertNull(result);
        verify(adminCategoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_정상삭제() {
        // given
        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId(200L);
        updateDto.setAdminId("admin3");

        CategoryDto origin = CategoryDto.builder()
                .categoryId(200L)
                .categoryName("카테고리2")
                .dltYsno("N")
                .build();

        when(adminCategoryRepository.findById(200L)).thenReturn(Optional.of(origin));
        when(adminCategoryRepository.save(any(CategoryDto.class))).thenReturn(origin);

        // when
        CategoryDto result = adminCategoryService.deleteCategory(updateDto);

        // then
        assertNotNull(result);
        assertEquals("Y", result.getDltYsno());
        assertEquals("admin3", result.getAmnrId());
        assertNotNull(result.getAmndDttm());
        verify(adminCategoryRepository).save(origin);
    }

    @Test
    void deleteCategory_없는카테고리() {
        // given
        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId(888L);
        when(adminCategoryRepository.findById(888L)).thenReturn(Optional.empty());

        // when
        CategoryDto result = adminCategoryService.deleteCategory(updateDto);

        // then
        assertNull(result);
        verify(adminCategoryRepository, never()).save(any());
    }

    @Test
    void searchCategories_동적조건조회() {
        // given
        CategorySearchDto searchDto = new CategorySearchDto();
        searchDto.setCategoryName("테스트");
        searchDto.setCategoryOrder(3);
        searchDto.setAdminId("admin4");

        CategoryDto c1 = CategoryDto.builder()
                .categoryId(1L)
                .categoryName("테스트카테고리")
                .categoryOrder(3)
                .crtrId("admin4")
                .dltYsno("N")
                .build();

        when(mongoTemplate.find(any(Query.class), eq(CategoryDto.class)))
                .thenReturn(Arrays.asList(c1));

        // when
        List<CategoryDto> result = adminCategoryService.searchCategories(searchDto);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("테스트카테고리", result.get(0).getCategoryName());

        // 쿼리 내부 조건 검증 (선택)
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(CategoryDto.class));
        Query usedQuery = queryCaptor.getValue();
        assertTrue(usedQuery.toString().contains("categoryName"));
        assertTrue(usedQuery.toString().contains("categoryOrder"));
        assertTrue(usedQuery.toString().contains("crtrId"));
    }

}
