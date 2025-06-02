package kr.co.module.mapper.repository;

import kr.co.module.core.dto.domain.ReservationDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReservationRepository extends MongoRepository<ReservationDto, Long> {

}
