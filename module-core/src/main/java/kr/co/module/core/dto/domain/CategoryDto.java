package kr.co.module.core.dto.domain;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    //카테고리 id
    private Long categoryId;
    //카테고리명
    private String categoryName;
    //카테고리 설명
    private String categoryDesc;
    //카테고리 정렬
    private Integer categoryOrder;

    private String crtrId;

    private LocalDateTime cretDttm;

    private String amnrId;

    private LocalDateTime amndDttm;

    private String dltYsno;

}


