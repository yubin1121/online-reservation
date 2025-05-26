package kr.co.module.api.admin.query.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchDto {

    //카테고리명
    private String categoryName;

    private Integer categoryOrder;

    private String adminId;
}
