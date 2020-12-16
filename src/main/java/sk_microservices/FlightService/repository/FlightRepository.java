package sk_microservices.FlightService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk_microservices.FlightService.entites.Airplane;
import sk_microservices.FlightService.entites.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {


}
