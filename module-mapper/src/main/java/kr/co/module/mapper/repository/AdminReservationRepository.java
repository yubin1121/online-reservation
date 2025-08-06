package kr.co.module.mapper.repository;

import kr.co.module.core.domain.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminReservationRepository extends MongoRepository<Reservation, String> {
    Optional<Reservation> findByReservationBizId(String reservationBizId);
}
