package kr.co.module.api.Service;
import kr.co.module.api.user.dto.ProductSearchDto;
import kr.co.module.api.user.service.ProductQueryService;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.mapper.repository.AdminProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserProductServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AdminProductRepository adminProductRepository;

    @InjectMocks
    private ProductQueryService productQueryService;

    private ProductDto product;

    @BeforeEach
    void setup() {
        product = ProductDto.builder()
                .productId("p1")
                .categoryId("c1")
                .productName("테스트상품")
                .productPlace("서울")
                .dltYsno("N")
                .build();
    }

    @Test
    void searchProducts_기본조건_조회() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(Collections.singletonList(product));

        // when
        List<ProductDto> result = productQueryService.searchProducts(searchDto);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo("p1");

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(ProductDto.class));
        String queryString = queryCaptor.getValue().toString();
        assertThat(queryString).contains("dltYsno");
    }

    @Test
    void searchProducts_관리자_카테고리_상품명_장소_조회() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setProductAdminId("admin1");
        searchDto.setCategoryId("c1");
        searchDto.setProductName("테스트");
        searchDto.setProductPlace("서울");

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(List.of(product));

        // when
        List<ProductDto> result = productQueryService.searchProducts(searchDto);

        // then
        assertThat(result).hasSize(1);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(ProductDto.class));
        String queryString = queryCaptor.getValue().toString();
        assertThat(queryString).contains("crtrId");
        assertThat(queryString).contains("categoryId");
        assertThat(queryString).contains("productName");
        assertThat(queryString).contains("productPlace");
    }

    @Test
    void searchProducts_날짜범위_조회() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setSrchFromDate("2024-06-01");
        searchDto.setSrchToDate("2024-06-30");

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(List.of(product));

        // when
        List<ProductDto> result = productQueryService.searchProducts(searchDto);

        // then
        assertThat(result).hasSize(1);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(ProductDto.class));
        String queryString = queryCaptor.getValue().toString();
        assertThat(queryString).contains("productAvlbDateList");
        assertThat(queryString).contains("$gte");
        assertThat(queryString).contains("$lte");
    }

    @Test
    void searchProducts_시간범위_조회() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setSrchFromTime("09:00");
        searchDto.setSrchToTime("18:00");

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(List.of(product));

        // when
        List<ProductDto> result = productQueryService.searchProducts(searchDto);

        // then
        assertThat(result).hasSize(1);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(ProductDto.class));
        String queryString = queryCaptor.getValue().toString();
        assertThat(queryString).contains("productAvlbTimeSlots");
        assertThat(queryString).contains("$gte");
        assertThat(queryString).contains("$lte");
    }

    @Test
    void searchProducts_결과없음() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setProductName("없는상품");

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(Collections.emptyList());

        // when
        List<ProductDto> result = productQueryService.searchProducts(searchDto);

        // then
        assertThat(result).isEmpty();
    }
}
