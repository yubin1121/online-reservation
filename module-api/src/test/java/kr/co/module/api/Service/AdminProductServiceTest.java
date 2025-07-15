package kr.co.module.api.Service;
import kr.co.module.api.admin.dto.*;
import kr.co.module.api.admin.service.AdminProductService;
import kr.co.module.core.domain.Product;
import kr.co.module.mapper.repository.AdminProductRepository;
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

public class AdminProductServiceTest {
    private MongoTemplate mongoTemplate;
    private AdminProductRepository adminProductRepository;
    private AdminProductService adminProductService;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        adminProductRepository = mock(AdminProductRepository.class);
        adminProductService = new AdminProductService(mongoTemplate, adminProductRepository);
    }

    @Test
    void searchMyProducts_정상조회() {
        // given
        AdminProductSearchDto searchDto = new AdminProductSearchDto();
        searchDto.setAdminId("admin1");

        Product p1 = Product.builder()
                .id("1")
                .productName("상품1")
                .crtrId("admin1")
                .build();

        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(Arrays.asList(p1));

        // when
        List<Product> result = adminProductService.searchMyProducts(searchDto);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("상품1", result.get(0).getProductName());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query usedQuery = queryCaptor.getValue();
        assertTrue(usedQuery.toString().contains("adminId"));
    }

    @Test
    void createProduct_정상생성() {
        // given
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setCategoryId("10");
        createDto.setProductName("신상품");
        createDto.setProductDesc("설명");
        createDto.setProductPlace("장소");
        createDto.setProductLocation("위치");
        createDto.setAdminId("admin2");
        createDto.setTotalQuantity(100);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        // when
        Product result = adminProductService.createProduct(createDto);

        // then
        verify(adminProductRepository, times(1)).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals("신상품", saved.getProductName());
        assertEquals("admin2", saved.getCrtrId());
        assertEquals("N", saved.getDltYsno());
        assertNotNull(result);
    }

    @Test
    void updateProduct_정상수정() {
        // given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setProductId("100");
        updateDto.setProductName("수정상품");
        updateDto.setAdminId("admin3");

        Product origin = Product.builder()
                .id("100")
                .productName("기존상품")
                .crtrId("admin3")
                .dltYsno("N")
                .build();

        when(adminProductRepository.findById("100")).thenReturn(Optional.of(origin));
        when(adminProductRepository.save(any(Product.class))).thenReturn(origin);

        // when
        Product result = adminProductService.updateProduct(updateDto);

        // then
        assertNotNull(result);
        assertEquals("수정상품", result.getProductName());
        assertEquals("admin3", result.getCrtrId());
        assertNotNull(result.getAmndDttm());
        verify(adminProductRepository).save(origin);
    }

    @Test
    void updateProduct_권한없음_수정불가() {
        // given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setProductId("200");
        updateDto.setProductName("수정상품");
        updateDto.setAdminId("adminX");

        Product origin = Product.builder()
                .id("200")
                .productName("기존상품")
                .crtrId("adminY")
                .dltYsno("N")
                .build();

        when(adminProductRepository.findById("200")).thenReturn(Optional.of(origin));

        // when
        Product result = adminProductService.updateProduct(updateDto);

        // then
        assertNull(result);
        verify(adminProductRepository, never()).save(any());
    }

    @Test
    void deleteProduct_정상삭제() {
        // given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setProductId("300");
        updateDto.setAdminId("admin4");

        Product origin = Product.builder()
                .id("300")
                .productName("상품삭제")
                .crtrId("admin4")
                .dltYsno("N")
                .build();

        when(adminProductRepository.findById("300")).thenReturn(Optional.of(origin));
        when(adminProductRepository.save(any(Product.class))).thenReturn(origin);

        // when
        Product result = adminProductService.deleteProduct(updateDto);

        // then
        assertNotNull(result);
        assertEquals("Y", result.getDltYsno());
        assertNotNull(result.getAmndDttm());
        verify(adminProductRepository).save(origin);
    }

    @Test
    void deleteProduct_권한없음_삭제불가() {
        // given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setProductId("400");
        updateDto.setAdminId("adminX");

        Product origin = Product.builder()
                .id("400")
                .productName("상품삭제")
                .crtrId("adminY")
                .dltYsno("N")
                .build();

        when(adminProductRepository.findById("400")).thenReturn(Optional.of(origin));

        // when
        Product result = adminProductService.deleteProduct(updateDto);

        // then
        assertNull(result);
        verify(adminProductRepository, never()).save(any());
    }
}
