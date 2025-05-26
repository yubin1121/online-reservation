package kr.co.module.api.admin.command.service;

import kr.co.module.api.admin.command.dto.ProductCreateDto;
import kr.co.module.api.admin.command.dto.ProductUpdateDto;
import kr.co.module.core.dto.domain.ProductDto;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductCommandService {

    @Getter
    private final List<ProductDto> productList = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // 상품 생성
    public boolean createProduct(ProductCreateDto productCreateDto) {
        ProductDto newProduct = new ProductDto();
        newProduct.setProductId(idGenerator.getAndIncrement());
        newProduct.setCategoryId(productCreateDto.getCategoryId());
        newProduct.setProductName(productCreateDto.getProductName());
        newProduct.setProductDesc(productCreateDto.getProductDesc());
        newProduct.setProductPlace(productCreateDto.getProductPlace());
        newProduct.setProductLocation(productCreateDto.getProductLocation());
        newProduct.setProductImgList(productCreateDto.getProductImgList());
        newProduct.setProductAvlbDateList(productCreateDto.getProductAvlbDateList());
        newProduct.setProductAvlbTimeSlots(productCreateDto.getProductAvlbTimeSlots());
        newProduct.setProductAvlbMaxPerSlots(productCreateDto.getProductAvlbMaxPerSlots());
        newProduct.setTotalQuantity(productCreateDto.getTotalQuantity());
        newProduct.setCrtrId(productCreateDto.getAdminId());
        newProduct.setCretDttm(LocalDateTime.now());
        newProduct.setAmnrId(productCreateDto.getAdminId());
        newProduct.setAmndDttm(LocalDateTime.now());
        newProduct.setDltYsno("N");
        productList.add(newProduct);

        System.out.println(newProduct);
        return true;
    }

    // 상품 수정
    public boolean updateProduct(ProductUpdateDto productUpdateDto) {
        Optional<ProductDto> productOpt = productList.stream()
                .filter(p -> p.getProductId().equals(productUpdateDto.getProductId()))
                .filter(p -> p.getCrtrId().equals(productUpdateDto.getAdminId()))
                .findFirst();
        if (productOpt.isPresent()) {
            ProductDto product = productOpt.get();
            if (productUpdateDto.getCategoryId() != null) product.setCategoryId(productUpdateDto.getCategoryId());
            if (productUpdateDto.getProductName() != null && !productUpdateDto.getProductName().isBlank()) product.setProductName(productUpdateDto.getProductName());
            if (productUpdateDto.getProductDesc() != null && !productUpdateDto.getProductDesc().isBlank()) product.setProductDesc(productUpdateDto.getProductDesc());
            if (productUpdateDto.getProductPlace() != null && !productUpdateDto.getProductPlace().isBlank()) product.setProductPlace(productUpdateDto.getProductPlace());
            if (productUpdateDto.getProductLocation() != null && !productUpdateDto.getProductLocation().isBlank()) product.setProductLocation(productUpdateDto.getProductLocation());
            if (productUpdateDto.getProductImgList() != null) product.setProductImgList(productUpdateDto.getProductImgList());
            if (productUpdateDto.getProductAvlbDateList() != null) product.setProductAvlbDateList(productUpdateDto.getProductAvlbDateList());
            if (productUpdateDto.getProductAvlbTimeSlots() != null) product.setProductAvlbTimeSlots(productUpdateDto.getProductAvlbTimeSlots());
            if (productUpdateDto.getProductAvlbMaxPerSlots() != null) product.setProductAvlbMaxPerSlots(productUpdateDto.getProductAvlbMaxPerSlots());
            if (productUpdateDto.getTotalQuantity() != null) product.setTotalQuantity(productUpdateDto.getTotalQuantity());
            product.setAmndDttm(LocalDateTime.now());
            return true;
        }
        return false;
    }

    // 상품 삭제
    public boolean deleteProduct(ProductUpdateDto productUpdateDto) {
        Optional<ProductDto> productOpt = productList.stream()
                .filter(p -> p.getProductId().equals(productUpdateDto.getProductId()))
                .filter(p -> p.getCrtrId().equals(productUpdateDto.getAdminId()))
                .findFirst();
        if (productOpt.isPresent()) {
            productOpt.get().setDltYsno("Y");
            productOpt.get().setAmndDttm(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
