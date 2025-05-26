package kr.co.module.api.admin.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDto {
    @NotNull(message = "상품ID는 필수입니다.")
    private Long productId;
    //카테고리 아이디
    private Long categoryId;
    private String productName;
    private String productDesc;
    private String productPlace;
    private String productLocation;
    private List<String> productImgList;
    private List<String> productAvlbDateList;
    private Map<String, List<String>> productAvlbTimeSlots; //
    private Map<String, Integer> productAvlbMaxPerSlots;
    private Integer totalQuantity;

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;
}
