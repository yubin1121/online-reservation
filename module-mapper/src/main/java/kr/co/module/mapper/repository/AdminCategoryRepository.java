package kr.co.module.mapper.repository;

import jakarta.validation.constraints.NotBlank;
import kr.co.module.core.dto.domain.CategoryDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminCategoryRepository extends MongoRepository<CategoryDto, String> {

    boolean existsByCategoryName(@NotBlank String categoryName);
}
