package kr.co.module.api.Service;

import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.dto.ReservationSearchDto;
import kr.co.module.api.user.dto.ReservationUpdateDto;
import kr.co.module.api.user.service.UserReservationService;
import kr.co.module.core.dto.domain.ProductDto;
import kr.co.module.core.dto.domain.ReservationDto;
import kr.co.module.core.exception.InsufficientStockException;
import kr.co.module.core.exception.ProductNotFoundException;
import kr.co.module.core.status.ReservationStatus;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.UserReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserReservationServiceTest {
    @Mock
    private AdminProductRepository productRepository;

    @Mock
    private UserReservationRepository reservationRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private UserReservationService reservationService;

    private ProductDto testProduct;
    private ReservationDto testReservation;
    private final String USER_ID = "user123";
    private final String PRODUCT_ID = "prod456";
    private final String RESERVATION_ID = "resv789";

    @BeforeEach
    void setUp() {
        testProduct = ProductDto.builder()
                .productId(PRODUCT_ID)
                .totalQuantity(10)
                .dltYsno("N")
                .build();

        testReservation = ReservationDto.builder()
                .reservationId(RESERVATION_ID)
                .userId(USER_ID)
                .productId(PRODUCT_ID)
                .reservationCnt(2)
                .reservationStatus(ReservationStatus.PENDING.name())
                .build();
    }

    @Test
    void reserve_Success() {
        // Given
        ReservationRequestDto request = new ReservationRequestDto(
                PRODUCT_ID, USER_ID,
                LocalDate.now().toString(), "14:00", 2
        );

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(reservationRepository.save(any())).thenReturn(testReservation);

        // When
        ReservationDto result = reservationService.reserve(request);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getReservationCnt());
        verify(productRepository).findById(PRODUCT_ID);
        verify(reservationRepository).save(any());
    }

    @Test
    void reserve_ProductNotFound() {
        // Given
        ReservationRequestDto request = new ReservationRequestDto(
                "invalid_id", USER_ID,
                LocalDate.now().toString(), "14:00", 2
        );

        when(productRepository.findById("invalid_id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
                () -> reservationService.reserve(request));
    }

    @Test
    void reserve_InsufficientStock() {
        // Given
        ReservationRequestDto request = new ReservationRequestDto(
                PRODUCT_ID, USER_ID,
                LocalDate.now().toString(), "14:00", 15
        );

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThrows(InsufficientStockException.class,
                () -> reservationService.reserve(request));
    }

    @Test
    void updateReservation_Success() {
        when(mongoTemplate.updateFirst(any(), any(), eq(ProductDto.class)))
                .thenReturn(null);

        // Given
        ReservationUpdateDto updateDto = new ReservationUpdateDto(
                RESERVATION_ID, USER_ID,
                LocalDate.now().plusDays(1).toString(), "15:00",
                3, ReservationStatus.PENDING.name()
        );

        testReservation.setReservationStatus(ReservationStatus.PENDING.name());
        testProduct.setTotalQuantity(10);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(testReservation));
        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(testProduct));

        when(reservationRepository.save(any(ReservationDto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReservationDto result = reservationService.updateReservation(updateDto);

        // Then
        assertNotNull(result); // null 체크 추가
        assertEquals("15:00", result.getReservationTime());
        assertEquals(3, result.getReservationCnt());
        assertEquals(ReservationStatus.CONFIRMED.name(), result.getReservationStatus());
    }

    @Test
    void updateReservation_UnauthorizedUser() {
        // Given
        ReservationUpdateDto updateDto = new ReservationUpdateDto(
                RESERVATION_ID, "hacker",
                null, null, null, null
        );

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(testReservation));

        // When & Then
        assertThrows(SecurityException.class,
                () -> reservationService.updateReservation(updateDto));
    }

    @Test
    void cancelReservation_Success() {
        // Given
        ReservationUpdateDto cancelDto = new ReservationUpdateDto(
                RESERVATION_ID, USER_ID, null, null, null, null
        );

        testReservation.setReservationStatus(ReservationStatus.PENDING.name());

        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(testProduct));
        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(ProductDto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReservationDto result = reservationService.cancelReservation(cancelDto);

        // Then
        assertNotNull(result);
        assertEquals(ReservationStatus.CANCELED.name(), result.getReservationStatus());

        // 상품 저장 호출 검증
        verify(productRepository).save(testProduct);
    }

    @Test
    void searchUserReservations_FilterByCategory() {
        // Given
        ReservationSearchDto searchDto = new ReservationSearchDto(
                USER_ID, "category123",
                LocalDate.now().minusDays(7).toString(), LocalDate.now().toString()
        );

        // When & Then
        assertDoesNotThrow(() -> reservationService.searchUserReservations(searchDto));
    }

    @Test
    void updateReservation_InvalidStatusChange() {
        // Given
        testReservation.setReservationStatus(ReservationStatus.CANCELED.name());
        ReservationUpdateDto updateDto = new ReservationUpdateDto(
                RESERVATION_ID, USER_ID, null, null, null, null
        );

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(testReservation));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservation(updateDto));
    }
}
