package kr.co.module.mapper.repository;

import kr.co.module.core.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends MongoRepository<Product, String> {

}
