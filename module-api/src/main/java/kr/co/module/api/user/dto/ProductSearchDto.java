package kr.co.module.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {
    //상품 관리자 아이디
    private String productAdminId;
    //카테고리 아이디
    private String categoryId;
    //상품명
    private String productName;
    //상품 장소
    private String productPlace;

    private String srchFromDate;
    private String srchToDate;

    private String srchFromTime;
    private String srchToTime;
}
