package kr.co.module.api.admin.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDto {

    //카테고리리 명
    @NotBlank
    private String categoryName;
    //카테고리 설명
    private String categoryDesc;
    //카테고리 정렬
    private Integer categoryOrder;

    @NotBlank(message = "관리자 ID는 필수입니다.")
    private String adminId;



}
