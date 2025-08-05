package kr.co.module.api.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async; // <-- @Async 어노테이션 추가
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture; // CompletableFuture 사용을 위해 임포트
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ImageUploadService {


    @Value("${upload.path}")
    private String uploadPath;

    private final Executor productTaskExecutor; // <-- Inject the Executor

    public ImageUploadService(@Qualifier("productTaskExecutor") Executor productTaskExecutor) {
        this.productTaskExecutor = productTaskExecutor;
    }

    @Async("productTaskExecutor")
    public CompletableFuture<List<String>> uploadProductImagesAsync(List<MultipartFile> files, String productId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Image upload started on thread: {}", Thread.currentThread().getName());
            List<String> imageUrls = new ArrayList<>();
            if (files == null || files.isEmpty()) {
                return imageUrls;
            }
            try {
                Path uploadDir = Paths.get(uploadPath, "products", productId);
                Files.createDirectories(uploadDir);

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        String fileExtension = "";
                        if (originalFilename != null && originalFilename.contains(".")) {
                            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        }
                        String savedFileName = UUID.randomUUID().toString() + fileExtension;
                        Path filePath = uploadDir.resolve(savedFileName);

                        Files.copy(file.getInputStream(), filePath);

                        String imageUrl = "/images/products/" + productId + "/" + savedFileName;
                        imageUrls.add(imageUrl);
                        log.info("Uploaded image: {} for product: {}", savedFileName, productId);
                    }
                }
                log.info("Image upload completed on thread: {}", Thread.currentThread().getName());
                return imageUrls;
            } catch (IOException e) {
                log.error("Failed to upload images for product {}: {}", productId, e.getMessage());
                throw new RuntimeException(e);
            }
        }, productTaskExecutor);
    }

    @Async("productTaskExecutor")
    public CompletableFuture<Void> deleteProductImagesAsync(List<String> imageUrls) {
        return CompletableFuture.runAsync(() -> {
            log.info("Image deletion started on thread: {}", Thread.currentThread().getName());

            if (imageUrls == null || imageUrls.isEmpty()) {
                log.info("No image URLs to delete.");
                return;
            }

            for (String url : imageUrls) {
                try {
                    Path filePathToDelete = Paths.get(uploadPath, url.replace("/images/", "")); // 실제 서버 경로로 변환
                    Files.deleteIfExists(filePathToDelete);
                    log.info("Deleted image: {}", url);
                } catch (IOException e) {
                    log.error("Failed to delete image {}: {}", url, e.getMessage());
                } catch (Exception e) {
                    log.error("An unexpected error occurred while deleting image {}: {}", url, e.getMessage());
                }
            }
            log.info("Image deletion completed on thread: {}", Thread.currentThread().getName());
        }, productTaskExecutor);
    }
}