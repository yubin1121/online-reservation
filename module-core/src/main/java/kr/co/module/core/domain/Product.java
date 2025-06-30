package kr.co.module.core.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product")
public class Product {
    //상품 아이디
    @Id
    private String productId;
    //카테고리 아이디
    private String categoryId;
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

    @CreatedDate
    private LocalDateTime cretDttm;

    private String amnrId;

    @LastModifiedDate
    private LocalDateTime amndDttm;

    private String dltYsno;
}
