package kr.co.module.api.admin.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDto {

    //카테고리 아이디
    @NotNull(message = "카테고리선택은 필수입니다.")
    private Long categoryId;
    //상품명
    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;
    //상품 설명
    @NotBlank(message = "상품설명은 필수입니다.")
    private String productDesc;
    //상품 장소
    @NotBlank(message = "위치는 필수입니다.")
    private String productPlace;
    //상품 위치
    @NotBlank(message = "위치는 필수입니다.")
    private String productLocation;
    //상품 이미지 리스트
    private List<String> productImgList;
    //상품 이용 가능한 날짜
    private List<String> productAvlbDateList;
    //상품 이용 가능한 날짜별 시간대
    private Map<String, List<String>> productAvlbTimeSlots; //
    // 최대 예약 가능 인원
    private Map<String, Integer> productAvlbMaxPerSlots;
    // 전체 예약 가능 수량
    private Integer totalQuantity;

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;
}
