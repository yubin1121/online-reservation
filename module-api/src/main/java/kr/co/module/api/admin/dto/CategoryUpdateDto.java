package kr.co.module.api.admin.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateDto {

    private String categoryId;
    private String categoryDesc; // null로 오면 설명 삭제
    private Integer categoryOrder;
    private String adminId;

}
