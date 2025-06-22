package kr.co.module.mapper.repository;

import kr.co.module.core.dto.domain.ProductDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminProductRepository extends MongoRepository<ProductDto, String> {

    boolean existsByProductIdAndCrtrId(String productId, String adminId);
}
