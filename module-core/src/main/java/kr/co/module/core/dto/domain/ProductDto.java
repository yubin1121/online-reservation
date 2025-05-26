package kr.co.module.core.dto.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    //상품 아이디
    private Long productId;
    //카테고리 아이디
    private Long categoryId;
    //상품명
    private String productName;
    //상품 설명
    private String productDesc;
    //상품 장소
    private String productPlace;
    //상품 위치
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

    private String crtrId;

    private LocalDateTime cretDttm;

    private String amnrId;

    private LocalDateTime amndDttm;

    private String dltYsno;
}
