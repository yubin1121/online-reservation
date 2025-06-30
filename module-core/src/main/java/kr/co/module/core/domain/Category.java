package kr.co.module.core.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "category")
public class Category {
    //카테고리 id
    @Id
    private String categoryId;
    //카테고리명
    private String categoryName;
    //카테고리 설명
    private String categoryDesc;
    //카테고리 정렬
    private Integer categoryOrder;

    private String crtrId;

    @CreatedDate
    private LocalDateTime cretDttm;

    private String amnrId;

    @LastModifiedDate
    private LocalDateTime amndDttm;

    private String dltYsno;

}


