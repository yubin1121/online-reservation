package kr.co.module.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product")
public class ProductUpdateDto {
    @NotBlank
    private String productId;
    //카테고리 아이디
    private String categoryId;
    private String productName;
    private String productDesc;
    private String productPlace;
    private String productLocation;
    private List<String> productImgList;
    private List<MultipartFile> newProductImages;
    private List<String> productAvlbDateList;
    private Map<String, List<String>> productAvlbTimeSlots; //
    private Map<String, Integer> productAvlbMaxPerSlots;
    private Integer totalQuantity;

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;
}
