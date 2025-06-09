package kr.co.module.api.Service;

import kr.co.module.api.admin.dto.AdminReservationSearchDto;
import kr.co.module.api.admin.dto.AdminReservationUpdateDto;
import kr.co.module.api.admin.service.AdminReservationService;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.mapper.repository.AdminReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class AdminReservationServiceTest {
    private MongoTemplate mongoTemplate;
    private AdminReservationRepository adminReservationRepository;
    private AdminReservationService adminReservationService;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        adminReservationRepository = mock(AdminReservationRepository.class);
        adminReservationService = new AdminReservationService(adminReservationRepository, mongoTemplate);
    }

    @Test
    void updateReservationStatus_정상수정() {
        // given
        AdminReservationUpdateDto updateDto = new AdminReservationUpdateDto();
        updateDto.setReservationId("1");
        updateDto.setReservationStatus("CONFIRMED");
        updateDto.setAdminId("admin1");

        ReservationDto reservation = new ReservationDto();
        reservation.setReservationId("1");
        reservation.setReservationStatus("PENDING");

        when(adminReservationRepository.findById("1")).thenReturn(Optional.of(reservation));
        when(adminReservationRepository.save(any(ReservationDto.class))).thenReturn(reservation);

        // when
        ReservationDto result = adminReservationService.updateReservationStatus(updateDto);

        // then
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getReservationStatus());
        assertEquals("admin1", result.getAmnrId());
        assertNotNull(result.getAmndDttm());
        verify(adminReservationRepository).save(reservation);
    }

    @Test
    void updateReservationStatus_없는예약() {
        // given
        AdminReservationUpdateDto updateDto = new AdminReservationUpdateDto();
        updateDto.setReservationId("999");
        when(adminReservationRepository.findById("999")).thenReturn(Optional.empty());

        // when
        ReservationDto result = adminReservationService.updateReservationStatus(updateDto);

        // then
        assertNull(result);
        verify(adminReservationRepository, never()).save(any());
    }

    @Test
    void searchAdminReservations_조건조회() {
        // given
        AdminReservationSearchDto searchDto = new AdminReservationSearchDto();
        searchDto.setAdminId("admin2");
        searchDto.setProductId("10");
        searchDto.setCategoryId("20");

        ProductDto p1 = ProductDto.builder()
                .productId("10")
                .categoryId("20")
                .crtrId("admin2")
                .build();

        ReservationDto r1 = new ReservationDto();
        r1.setReservationId("100");
        r1.setProductId("10");

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(Arrays.asList(p1));
        when(mongoTemplate.find(any(Query.class), eq(ReservationDto.class)))
                .thenReturn(Arrays.asList(r1));

        // when
        List<ReservationDto> result = adminReservationService.searchAdminReservations(searchDto);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getReservationId());

        // 쿼리 내부 조건 검증 (선택)
        ArgumentCaptor<Query> prodQueryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(prodQueryCaptor.capture(), eq(ProductDto.class));
        Query usedProductQuery = prodQueryCaptor.getValue();
        assertTrue(usedProductQuery.toString().contains("crtrId"));
        assertTrue(usedProductQuery.toString().contains("productId"));
        assertTrue(usedProductQuery.toString().contains("categoryId"));

        ArgumentCaptor<Query> resvQueryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(resvQueryCaptor.capture(), eq(ReservationDto.class));
        Query usedReservationQuery = resvQueryCaptor.getValue();
        assertTrue(usedReservationQuery.toString().contains("productId"));
    }

    @Test
    void searchAdminReservations_상품없으면빈리스트() {
        // given
        AdminReservationSearchDto searchDto = new AdminReservationSearchDto();
        searchDto.setAdminId("admin3");
        searchDto.setProductId("0");
        searchDto.setCategoryId("0");

        when(mongoTemplate.find(any(Query.class), eq(ProductDto.class)))
                .thenReturn(Arrays.asList());

        // when
        List<ReservationDto> result = adminReservationService.searchAdminReservations(searchDto);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoTemplate, never()).find(any(Query.class), eq(ReservationDto.class));
    }
}
