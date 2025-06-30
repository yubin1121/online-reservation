package kr.co.module.mapper.repository;

import jakarta.validation.constraints.NotBlank;
import kr.co.module.core.domain.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminCategoryRepository extends MongoRepository<Category, String> {

    boolean existsByCategoryName(@NotBlank String categoryName);
}
