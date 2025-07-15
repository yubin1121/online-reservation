package kr.co.module.api.Service;

import kr.co.module.api.admin.dto.*;
import kr.co.module.api.admin.service.AdminCategoryService;
import kr.co.module.core.domain.Category;
import kr.co.module.mapper.repository.AdminCategoryRepository;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminCategoryServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    @Mock
    AdminCategoryRepository adminCategoryRepository;

    @AfterEach
    void tearDown() {
        reset(mongoTemplate, adminCategoryRepository);
    }

    @Test
    void createCategory_정상생성() {
        // given
        CategoryCreateDto createDto = new CategoryCreateDto();
        createDto.setCategoryName("카테고리1");
        createDto.setCategoryDesc("설명");
        createDto.setCategoryOrder(1);
        createDto.setAdminId("admin1");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        Category savedDto = new Category();
        savedDto.setCategoryName(createDto.getCategoryName());
        savedDto.setCategoryDesc(createDto.getCategoryDesc());
        savedDto.setCategoryOrder(createDto.getCategoryOrder());
        savedDto.setCrtrId(createDto.getAdminId());
        savedDto.setDltYsno("N");

        when(adminCategoryRepository.save(any(Category.class)))
                .thenReturn(savedDto);

        Category result = adminCategoryService.createCategory(createDto);

        verify(adminCategoryRepository, times(1)).save(captor.capture());
        Category saved = captor.getValue();
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
        updateDto.setCategoryId("100");
        updateDto.setCategoryDesc("수정된 설명");
        updateDto.setCategoryOrder(2);
        updateDto.setAdminId("admin2");

        Category origin = Category.builder()
                ._id("100")
                .categoryName("카테고리1")
                .categoryDesc("기존 설명")
                .categoryOrder(1)
                .crtrId("admin2")
                .dltYsno("N")
                .build();

        when(adminCategoryRepository.findById("100")).thenReturn(Optional.of(origin));
        when(adminCategoryRepository.save(any(Category.class))).thenReturn(origin);

        // when
        Category result = adminCategoryService.updateCategory(updateDto);

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
        updateDto.setCategoryId("999");
        when(adminCategoryRepository.findById("999")).thenReturn(Optional.empty());

        // when
        Category result = adminCategoryService.updateCategory(updateDto);

        // then
        assertNull(result);
        verify(adminCategoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_정상삭제() {
        // given
        CategoryUpdateDto updateDto = new CategoryUpdateDto();
        updateDto.setCategoryId("200");
        updateDto.setAdminId("admin3");

        Category origin = Category.builder()
                ._id("200")
                .categoryName("카테고리2")
                .dltYsno("N")
                .build();

        when(adminCategoryRepository.findById("200")).thenReturn(Optional.of(origin));
        when(adminCategoryRepository.save(any(Category.class))).thenReturn(origin);

        // when
        Category result = adminCategoryService.deleteCategory(updateDto);

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
        updateDto.setCategoryId("888");
        when(adminCategoryRepository.findById("888")).thenReturn(Optional.empty());

        // when
        Category result = adminCategoryService.deleteCategory(updateDto);

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

        Category c1 = Category.builder()
                ._id("100")
                .categoryName("테스트카테고리")
                .categoryOrder(3)
                .crtrId("admin4")
                .dltYsno("N")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.find(any(Query.class), eq(Category.class)))
                .thenReturn(Collections.singletonList(c1));

        // when
        Page<Category> result = adminCategoryService.searchCategories(searchDto, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("테스트카테고리", result.getContent().get(0).getCategoryName());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Category.class));
        Query usedQuery = queryCaptor.getValue();

        Document queryDoc = usedQuery.getQueryObject();

        // categoryName 검증
        assertTrue(queryDoc.get("categoryName") instanceof Pattern);
        Pattern namePattern = (Pattern) queryDoc.get("categoryName");
        assertEquals("테스트", namePattern.pattern());

        // categoryOrder 검증
        assertEquals(3, queryDoc.getInteger("categoryOrder"));

        // crtrId 검증
        assertEquals("admin4", queryDoc.getString("crtrId"));

        // dltYsno 타입
        assertTrue(queryDoc.get("dltYsno") instanceof String);
        assertEquals("N", queryDoc.get("dltYsno"));
    }


}
