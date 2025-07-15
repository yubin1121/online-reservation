package kr.co.module.api.Service;

import kr.co.module.api.admin.dto.AdminReservationSearchDto;
import kr.co.module.api.admin.dto.AdminReservationUpdateDto;
import kr.co.module.api.admin.service.AdminReservationService;
import kr.co.module.core.domain.Product;
import kr.co.module.core.domain.Reservation;
import kr.co.module.core.exception.ReservationNotFoundException;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.AdminReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class AdminReservationServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AdminReservationService adminReservationService;

    @Mock
    AdminReservationRepository adminReservationRepository;
    @Mock
    AdminProductRepository adminProductRepository;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        adminReservationRepository = mock(AdminReservationRepository.class);
        adminProductRepository = mock(AdminProductRepository.class);

        adminReservationService = new AdminReservationService(mongoTemplate, adminReservationRepository, adminProductRepository);
    }

    @Test
    void updateReservationStatus_정상수정() {
        // given
        AdminReservationUpdateDto updateDto = new AdminReservationUpdateDto();
        updateDto.setReservationId("1");
        updateDto.setReservationStatus("CONFIRMED");
        updateDto.setAdminId("admin1");

        Reservation reservation = new Reservation();
        reservation.set_id("1");
        reservation.setReservationStatus("PENDING");
        reservation.setProductId("product123");  // 추가: 상품 ID 설정

        Product product = new Product();
        product.set_id("product123");
        product.setCrtrId("admin1");  // 상품 생성자를 admin1로 설정

        when(adminReservationRepository.findById("1")).thenReturn(Optional.of(reservation));
        when(adminProductRepository.existsByProductIdAndCrtrId("product123", "admin1")).thenReturn(true);  // 권한 검증 통과
        when(adminReservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        Reservation result = adminReservationService.updateReservationStatus(updateDto);

        // then
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getReservationStatus());
        verify(adminReservationRepository).save(reservation);

    }

    @Test
    void updateReservationStatus_없는예약() {
        // given
        AdminReservationUpdateDto updateDto = new AdminReservationUpdateDto();
        updateDto.setReservationId("999");
        when(adminReservationRepository.findById("999")).thenReturn(Optional.empty());

        // when & then
        assertThrows(ReservationNotFoundException.class,
                () -> adminReservationService.updateReservationStatus(updateDto));

        verify(adminReservationRepository, never()).save(any());
    }

    @Test
    void searchAdminReservations_조건조회() {
        // given
        AdminReservationSearchDto searchDto = new AdminReservationSearchDto();
        searchDto.setAdminId("admin2");
        searchDto.setProductId("10");
        searchDto.setCategoryId("20");

        Product p1 = Product.builder()
                ._id("10")
                .categoryId("20")
                .crtrId("admin2")
                .build();

        Reservation r1 = new Reservation();
        r1.set_id("100");
        r1.setProductId("10");

        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(Collections.singletonList(p1));
        when(mongoTemplate.find(any(Query.class), eq(Reservation.class)))
                .thenReturn(List.of(r1));

        // when
        List<Reservation> result = adminReservationService.searchAdminReservations(searchDto);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("100", result.get(0).get_id());

    }

    @Test
    void searchAdminReservations_상품없으면빈리스트() {
        // given
        AdminReservationSearchDto searchDto = new AdminReservationSearchDto();
        searchDto.setAdminId("admin3");
        searchDto.setProductId("0");
        searchDto.setCategoryId("0");

        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(List.of());

        // when
        List<Reservation> result = adminReservationService.searchAdminReservations(searchDto);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoTemplate, never()).find(any(Query.class), eq(Reservation.class));
    }
}
