package sk_microservices.FlightService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sk_microservices.FlightService.entites.Airplane;
import sk_microservices.FlightService.entites.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsByAvion_Id(long id);

    @Query("select f.avion.kapacitetPutnika from Flight f where f.id = :id")
    int getCapacityForFlight(long id);

    @Query("select f.duzinaLeta from Flight f where f.id = :id")
    int getLengthForFlight(long id);

}
