package kr.co.module.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchDto {

    //카테고리명
    private String categoryName;

    private Integer categoryOrder;

    private String adminId;
}