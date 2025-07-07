package kr.co.module.api.Concurrency; // 테스트 클래스용 패키지

import kr.co.module.api.admin.service.AdminReservationService;
import kr.co.module.core.domain.Product;
import kr.co.module.core.domain.Reservation;
import kr.co.module.mapper.repository.AdminProductRepository;
import kr.co.module.mapper.repository.AdminReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev") // 개발 프로파일을 사용하여 DataLoader가 초기 데이터 삽입
public class ReservationConcurrencyTest {

    @Autowired
    private kr.co.module.mapper.repository.UserRepository userRepository;
    @Autowired
    private AdminProductRepository productRepository;
    @Autowired
    private AdminReservationRepository reservationRepository;
    @Autowired
    private AdminReservationService reservationService; // 예약 로직을 처리하는 서비스 (가정)

    private String testProductId;
    private String testUserId1;
    private String testUserId2;

    @BeforeEach
    void setUp() {
         reservationRepository.deleteAll();
        productRepository.deleteAll();

        // Category category = categoryRepository.save(Category.builder().categoryName("Test Category").build());

        // totalQuantity가 10인 상품 생성
        Product product = productRepository.save(Product.builder()
                ._id("product_test_001")
                .categoryId("some_category_id")
                .productName("Concurrency Test Product")
                .productDesc("Product for concurrency testing")
                .totalQuantity(10) // 최대 10개 예약 가능
                // 다른 필수 필드들도 채워야 함 (예: productPlace, productAvlbDateList 등)
                .productAvlbDateList(Arrays.asList(LocalDateTime.now().plusDays(1).toLocalDate().toString()))
                .productAvlbTimeSlots(Map.of(LocalDateTime.now().plusDays(1).toLocalDate().toString(), Arrays.asList("10:00")))
                .productAvlbMaxPerSlots(Map.of("10:00", 10))
                .crtrId("system")
                .cretDttm(LocalDateTime.now())
                .dltYsno("N")
                .build());

        testProductId = product.get_id();
    }

    @Test
    @DisplayName("N개의 스레드가 동시 예약 시도 시, 총 수량 제한을 초과하지 않는다")
    void testConcurrentReservations() throws InterruptedException {
        int numberOfThreads = 20; // 20개의 스레드가 동시에 예약 시도 (총 수량 10개 초과)
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        String reservationDate = LocalDateTime.now().plusDays(1).toLocalDate().toString();
        String reservationTime = "10:00";

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            String currentUserId = (threadNum % 2 == 0) ? testUserId1 : testUserId2;

            executorService.submit(() -> {
                try {
                    // 예약 서비스 호출 (실제 서비스 메소드 이름과 인자에 맞게 변경)
                    // 이 메소드가 Product의 totalQuantity를 감소시키고 Reservation을 생성한다고 가정
                    Reservation newReservation = reservationService.createReservation(
                            testProductId, currentUserId, reservationDate, reservationTime, 1
                    );
                    if (newReservation != null) {
                        successCount.incrementAndGet();
                        System.out.println("Thread " + threadNum + " - Reservation success: " + newReservation.get_id());
                    } else {
                        failCount.incrementAndGet();
                        System.out.println("Thread " + threadNum + " - Reservation failed (null returned)");
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("Thread " + threadNum + " - Reservation failed with exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 최종 검증
        long totalReservationsInDb = reservationRepository.count();
        Product finalProduct = productRepository.findById(testProductId).orElseThrow();

        System.out.println("--- Test Results ---");
        System.out.println("Attempts: " + numberOfThreads);
        System.out.println("Success Count (from service return): " + successCount.get());
        System.out.println("Fail Count (from service return): " + failCount.get());
        System.out.println("Total Reservations in DB: " + totalReservationsInDb);
        System.out.println("Final Product Quantity: " + finalProduct.getTotalQuantity()); // 총 수량이 아닌 남아있는 수량 필드가 있다면 그것 확인

        assertThat(totalReservationsInDb).isEqualTo(10);
        assertThat(totalReservationsInDb).isEqualTo(successCount.get()); // 서비스가 정확히 성공 반환했다면
          }
}