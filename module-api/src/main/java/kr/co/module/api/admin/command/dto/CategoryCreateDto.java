package kr.co.module.api.admin.command.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDto {

    //카테고리명
    @NotBlank(message = "카테고리명은 필수입니다.")
    private String categoryName;
    //카테고리 설명
    private String categoryDesc;
    //카테고리 정렬
    private Integer categoryOrder;

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;



}
