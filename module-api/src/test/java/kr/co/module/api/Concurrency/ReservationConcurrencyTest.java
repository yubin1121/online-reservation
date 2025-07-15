package kr.co.module.api.Concurrency; // 테스트 클래스용 패키지

import kr.co.module.api.user.dto.ReservationRequestDto;
import kr.co.module.api.user.service.UserReservationService;
import kr.co.module.core.domain.Product;
import kr.co.module.core.domain.Reservation;
import kr.co.module.core.exception.InsufficientStockException;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.UserReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev") // 개발 프로파일을 사용하여 DataLoader가 초기 데이터 삽입
public class ReservationConcurrencyTest {

    @Autowired
    private UserReservationService userReservationService;

    @Autowired
    private AdminProductRepository adminProductRepository; // Product 관리를 위해 필요

    @Autowired
    private UserReservationRepository userReservationRepository; // Reservation 조회 및 초기화를 위해 필요

    // 테스트에 필요한 변수들
    private String testProductId;
    private final int initialStock = 10; // 테스트용 초기 재고
    private final int numConcurrentRequests = 20; // 동시에 보낼 요청 수 (재고보다 많게 설정)
    private final int reservationCountPerRequest = 1; // 각 요청마다 예약할 수량

    // 테스트 사용자 ID (동시성 테스트 시 여러 사용자 시뮬레이션)
    private String testUserId1 = "user-alpha";
    private String testUserId2 = "user-beta";

    @BeforeEach
    void setUp() {
        userReservationRepository.deleteAll();
        adminProductRepository.deleteAll();
        log.info("--- Data Cleaned Up Before Test ---");

        // 테스트용 상품 생성 및 재고 설정
        Product product = Product.builder()
                .id("product_test_001") // ⭐ _id 대신 @Id로 매핑된 'id' 필드 사용
                .categoryId("some_category_id")
                .productName("Concurrency Test Product")
                .productDesc("Product for concurrency testing")
                .totalQuantity(initialStock) // 초기 재고 설정
                .productAvlbDateList(Collections.singletonList(LocalDate.now().plusDays(1).toString())) // 내일 날짜
                .productAvlbTimeSlots(Map.of(LocalDate.now().plusDays(1).toString(), Collections.singletonList("10:00")))
                .productAvlbMaxPerSlots(Map.of("10:00", initialStock)) // 슬롯당 최대 수량도 초기 재고와 같게 설정
                .crtrId("system")
                .cretDttm(LocalDateTime.now())
                .dltYsno("N")
                .build();
        adminProductRepository.save(product);
        testProductId = product.getId(); // ⭐ _id 대신 'id' 필드 사용
        log.info("Test Product created with ID: {} and initial stock: {}", testProductId, initialStock);

    }

    @AfterEach
    void tearDown() {
        // ⭐ 테스트 후 데이터 클린업
        userReservationRepository.deleteAll();
        adminProductRepository.deleteAll();
        log.info("--- Data Cleaned Up After Test ---");
    }

    @Test
    @DisplayName("N개의 스레드가 동시 예약 시도 시, 총 수량 제한을 초과하지 않고 원자성 보장")
    void testConcurrentReservations() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numConcurrentRequests);
        CountDownLatch latch = new CountDownLatch(numConcurrentRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        String reservationDateStr = LocalDate.now().plusDays(1).toString();
        String reservationTimeStr = LocalTime.of(10, 0).toString();
        log.info("Starting {} concurrent reservation requests for product {}...", numConcurrentRequests, testProductId);

        for (int i = 0; i < numConcurrentRequests; i++) {
            final int threadNum = i;
            // 사용자 ID를 번갈아 가며 사용 (혹은 고유하게 생성)
            String currentUserId = (threadNum % 2 == 0) ? testUserId1 : testUserId2;

            executorService.submit(() -> {
                try {
                    // 예약 요청 DTO 생성
                    ReservationRequestDto requestDto = ReservationRequestDto.builder()
                            .productId(testProductId)
                            .userId(currentUserId)
                            .reservationDate(reservationDateStr)
                            .reservationTime(reservationTimeStr)
                            .reservationCnt(reservationCountPerRequest)
                            .build();

                    // 예약 서비스 호출: userReservationService.reserve()
                    Reservation newReservation = userReservationService.reserve(requestDto);

                    // 예약 객체가 null이 아니고, ID가 있는지 확인
                    assertThat(newReservation).isNotNull();
                    assertThat(newReservation.getId()).isNotNull();

                    successCount.incrementAndGet();
                    log.debug("Thread {} (User {}) - Reservation success: {}", threadNum, currentUserId, newReservation.getId());

                } catch (InsufficientStockException e) {
                    failCount.incrementAndGet();
                    log.warn("Thread {} (User {}) - Reservation failed due to insufficient stock: {}", threadNum, currentUserId, e.getMessage());
                } catch (OptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                    log.warn("Thread {} (User {}) - Reservation failed due to optimistic locking conflict: {}", threadNum, currentUserId, e.getMessage());
                    // 낙관적 락 사용 시, 여기에 재시도 로직을 추가할 수 있습니다.
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("Thread {} (User {}) - An unexpected error occurred during reservation: {}", threadNum, currentUserId, e.getMessage(), e);
                } finally {
                    latch.countDown(); // 작업 완료를 알림
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();
        executorService.shutdown(); // 스레드 풀 종료
        // 스레드 풀의 모든 작업이 완료될 때까지 대기 (최대 1분)
        executorService.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);

        log.info("--- All Concurrent Requests Finished ---");
        log.info("Total Attempts: {}", numConcurrentRequests);
        log.info("Successful Reservations (from service return): {}", successCount.get());
        log.info("Failed Reservations (from service return): {}", failCount.get());

        // --- 최종 검증 ---

        // 1. 최종 상품 재고 확인
        Product finalProduct = adminProductRepository.findById(testProductId).orElse(null);
        assertThat(finalProduct).isNotNull();

        // 성공한 예약 수량만큼 재고가 정확히 감소했는지 검증
        int expectedFinalStock = initialStock - (successCount.get() * reservationCountPerRequest);
        assertThat(finalProduct.getTotalQuantity())
                .as("최종 재고는 성공한 예약 수량만큼 감소해야 합니다.")
                .isEqualTo(expectedFinalStock);

        // 재고가 음수가 되지 않았는지 확인 (InsufficientStockException으로 방어되었어야 함)
        assertThat(finalProduct.getTotalQuantity())
                .as("최종 재고는 0 이상이어야 합니다.")
                .isGreaterThanOrEqualTo(0);

        // 2. 최종 예약 문서 수 확인
        long finalReservationCountInDb = userReservationRepository.count();
        assertThat(finalReservationCountInDb)
                .as("최종 DB 예약 문서 수는 성공한 예약 요청 수와 일치해야 합니다.")
                .isEqualTo(successCount.get());

        // 3. (선택 사항) 실패한 요청 수가 예상과 일치하는지 확인
        // assertThat(failCount.get()).isEqualTo(numConcurrentRequests - initialStock);
        // 이 검증은 InsufficientStockException 외의 다른 예외가 발생하지 않았을 때 유효합니다.
    }
}