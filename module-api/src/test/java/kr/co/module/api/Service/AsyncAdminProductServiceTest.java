package kr.co.module.api.Service;

import kr.co.module.api.admin.dto.ProductCreateDto;
import kr.co.module.api.admin.service.AdminProductService;
import kr.co.module.api.common.service.ImageUploadService;
import kr.co.module.api.config.AsyncConfig;
import kr.co.module.core.domain.Product;
import kr.co.module.mapper.repository.AdminProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AsyncConfig.class, ImageUploadService.class})
class AsyncAdminProductServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AdminProductRepository adminProductRepository;

    @SpyBean
    private ImageUploadService imageUploadService;

    @InjectMocks
    private AdminProductService adminProductService;

    private AtomicReference<String> uploadThreadName;

    @BeforeEach
    void setUp() {
        uploadThreadName = new AtomicReference<>();

        when(adminProductRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    if (product.getId() == null) {
                        product.setId("testProductId123");
                    }
                    if (product.getCrtrId() == null) {
                        product.setCrtrId("testAdminId");
                    }
                    if (product.getCretDttm() == null) {
                        product.setCretDttm(LocalDateTime.now());
                    }
                    return product;
                });
    }

    @Test
    void createProduct_shouldInitiateImageUploadAsynchronously() {
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setProductName("Test Async Product");
        createDto.setAdminId("testAdminId");
        createDto.setProductImages(Arrays.asList(
                mock(MultipartFile.class), mock(MultipartFile.class)
        ));

        String callingThreadName = Thread.currentThread().getName();
        System.out.println("TEST: createProduct called on thread: " + callingThreadName);

        Product savedProduct = adminProductService.createProduct(createDto);

        System.out.println("TEST: createProduct returned on thread: " + Thread.currentThread().getName());

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isEqualTo("testProductId123");
        assertThat(savedProduct.getProductImgList()).isNull();

        verify(imageUploadService, timeout(2000)).uploadProductImagesAsync(anyList(), eq("testProductId123"));

        await().atMost(3, TimeUnit.SECONDS).until(() -> uploadThreadName.get() != null);
        System.out.println("TEST: Image upload method executed on thread: " + uploadThreadName.get());
        assertThat(uploadThreadName.get())
                .isNotNull()
                .startsWith("Product-Async-")
                .isNotEqualTo(callingThreadName);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(adminProductRepository, times(2)).save(productCaptor.capture());

            List<Product> capturedProducts = productCaptor.getAllValues();
            Product productAfterAsyncUpdate = capturedProducts.get(1);

            assertThat(productAfterAsyncUpdate.getProductImgList())
                    .containsExactly("http://mock.url/image1.jpg", "http://mock.url/image2.jpg");
        });

        System.out.println("TEST: All async operations verified successfully.");
    }
}