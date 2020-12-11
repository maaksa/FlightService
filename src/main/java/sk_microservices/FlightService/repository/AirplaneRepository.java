package sk_microservices.FlightService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk_microservices.FlightService.entites.Airplane;

@Repository
public interface AirplaneRepository extends JpaRepository<Airplane, Long> {


}
